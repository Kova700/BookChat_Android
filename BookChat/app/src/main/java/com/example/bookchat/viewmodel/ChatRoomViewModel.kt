package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.withTransaction
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Chat
import com.example.bookchat.data.local.dao.ChatDAO.Companion.MIN_CHAT_ID
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatStatus
import com.example.bookchat.data.local.entity.ChatEntity.ChatType
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.paging.remotemediator.ChatRemoteMediator
import com.example.bookchat.repository.ChatRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DateManager
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompSession

// TODO : 비즈니스 로직만 남기고 잡다한 요청은 Repository에 local, remote 구분해서 이전
class ChatRoomViewModel @AssistedInject constructor(
    @Assisted val chatRoomEntity: ChatRoomEntity,
    @Assisted private var firstEnterFlag: Boolean,
    private val chatRepository: ChatRepository
) : ViewModel() {
    val eventFlow = MutableSharedFlow<ChatEvent>()

    val inputtedMessage = MutableStateFlow("")
    private lateinit var stompSession: StompSession

    private val roomId = chatRoomEntity.roomId
    private val roomSId = chatRoomEntity.roomSid
    val database = App.instance.database

    var newOtherChatNoticeFlow = MutableStateFlow<ChatEntity?>(null)
    var isFirstItemOnScreen = true
    var scrollForcedFlag = false

    //TODO : 카톡은 소켓 통신의 생명 주기를 앱의 생명주기를 따르는 듯함
    // 데이터를 꺼두고, 채팅방 목록으로 돌아오고, 다시 데이터를 연결하면
    // 자동으로 채팅이 전송됨

    //TODO : 전송 대기 중이던 채팅 전송 실패 UI로 전환 구현해야함
    private var waitingChatList = listOf<ChatEntity>()

    @OptIn(ExperimentalPagingApi::class)
    val chatDataFlow = Pager(
        config = PagingConfig(pageSize = LOCAL_DATA_CHAT_LOAD_SIZE),
        remoteMediator = ChatRemoteMediator(
            database = database,
            chatRoomId = roomId,
            apiClient = App.instance.bookChatApiClient
        ),
        pagingSourceFactory = { database.chatDAO().pagingSource(roomId) }
    ).flow

    init {
        viewModelScope.launch {
            if (firstEnterFlag) sendEnterMessage() //이걸 집어넣을 마땅한 곳이 없음
            //collect는 join해서 기다릴 수 없음
            //채팅 Topic이 구독되면, 자동으로 입장 Send를 보내고 싶음
            //하지만 collect는 실패하지 않으면 아래 코드가 실행되지 않음
            connectSocket()
            observeChatFlowJob.start()
            observeErrorFlowJob.start()
            observeWaitingChatFlowJob.start()
        }
    }

    //TODO : 20분 간격으로 토큰 갱신 요청 보내는 로직 추가해야함 (화면이 꺼졌을 떄는 안보내는게 좋을듯) (더 좋은 방법 생각)
    //TODO : 채팅 켜둔채로 화면 꺼졌다가 다시 들어왔을때, 토큰이 만료되어서 채팅 전송이 실패할 수 도 있음 (예외처리)
    //TODO : 전송 대기중에 인터넷이 끊겨도, 인터넷이 연결되는 즉시 전송이 되어야함 (인터넷 연결상태를 observe하고 있어야함)
    //TODO : 요청 실패시 다시 실패했던 요청 재 전송해야함 (실패 이유별로 분기 필요)
    //TODO : 소켓 연결 끊기면 마지막 채팅으로 페이징 한 번 하고 다시 소켓 연결 (특정횟수만큼 소켓 재연결 요청)
    //TODO : 예외별로 예외처리

    private suspend fun connectSocket() {
        runCatching { chatRepository.getStompSession() }
            .onSuccess { stompSession = it }
            .onFailure { handleError(it) }
    }

    //TODO : 채팅방 생성하고, 입장 Send 안넘어옴 원인 파악하기,
    private val observeChatFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) {
            runCatching { collectChat() }
                .onFailure { handleError(it) }
        }

    private val observeErrorFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) {
            runCatching { collectSocketError() }
                .onFailure { handleError(it) }
        }

    private suspend fun collectChat() {
        val chatFlow = subscribeChatRoom(roomSId)
        if (firstEnterFlag) sendEnterMessage()
        chatFlow.collect { msg -> saveChatInLocalDB(msg.parseToChatEntity()) }
    }

    private suspend fun collectSocketError() {
        subscribeSocketError().collect { handleErrorFlow(it) }
    }

    private fun String.parseToChatEntity(): ChatEntity {
        return Gson().fromJson(this, Chat::class.java).toChatEntity(roomId)
    }

    private val observeWaitingChatFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) { collectWaitingChatList() }

    private suspend fun collectWaitingChatList() {
        database.chatDAO().getWaitingChatList(roomId).collect { waitingChatList = it }
    }

    //TODO : 누군가 입장했다 같은 이벤트도 구분해야함 (ChatRoomEntity에 인원 수 증가시켜야함)
    // 선택지1. 채팅Text끝부분에 입장했습니다.가 붙은 Chat이 오면 내가 수작업한다. (공지 채팅 내부에서 구분 Flag 추가)
    // 선택지2. 따로 채팅방 공지의 경우 따로 Topic을 파서 그거에 관련한 채팅만 주고 받는다 (그곳에서도 공지 타입을 구분해서 분기)
    private suspend fun saveChatInLocalDB(chat: ChatEntity) {
        val myUserId = App.instance.getCachedUser().userId
        database.withTransaction {
            if (chat.senderId != myUserId) {
                if (!isFirstItemOnScreen) newOtherChatNoticeFlow.value = chat
                insertNewChat(chat)
                updateChatRoomLastChatInfo(chat)
                return@withTransaction
            }

            //TODO : message로 비교하는거 상당히 별로인데.,, 다른값으로 비교 가능하면 수정
            for (waitingChat in waitingChatList) {
                if (waitingChat.message.length != chat.message.length) continue
                if (waitingChat.message != chat.message) continue
                updateChatInfo(chat, waitingChat.chatId)
                break
            }
            updateChatRoomLastChatInfo(chat)
        }
    }

    private suspend fun insertNewChat(chat: ChatEntity) {
        database.chatDAO().insertChat(chat)
    }

    private suspend fun updateChatInfo(chat: ChatEntity, targetChatId: Long) {
        database.chatDAO().updateChatInfo(
            chatId = chat.chatId,
            dispatchTime = chat.dispatchTime,
            status = ChatStatus.SUCCESS,
            targetChatId = targetChatId
        )
    }

    private suspend fun updateChatRoomLastChatInfo(chat: ChatEntity) {
        database.chatRoomDAO().updateLastChatInfo(
            roomId = chat.chatRoomId,
            lastChatId = chat.chatId,
            lastActiveTime = chat.dispatchTime,
            lastChatContent = chat.message
        )
    }

    private suspend fun subscribeChatRoom(roomSid: String): Flow<String> {
        runCatching { chatRepository.subscribeChatRoom(stompSession, roomSid) }
            .onSuccess { return it }
        throw Exception("Fail subscribeChatRoom")
    }

    private suspend fun subscribeSocketError(): Flow<String> {
        runCatching { chatRepository.subscribeErrorResponse(stompSession) }
            .onSuccess { return it }
        throw Exception("Fail subscribeSocketError")
    }

    fun sendMessage() = viewModelScope.launch(Dispatchers.IO) {
        if (inputtedMessage.value.isBlank()) return@launch

        scrollForcedFlag = true
        insertNewChat(getMineChatEntity(inputtedMessage.value))
        runCatching { chatRepository.sendMessage(stompSession, roomId, inputtedMessage.value) }
            .onSuccess { Log.d(TAG, "ChatRoomViewModel: sendMessage() - 메세지 전송 성공 !! $it") }
            .onFailure { handleError(it) }
            .also { inputtedMessage.value = "" }
    }

    private suspend fun getMineChatEntity(message: String): ChatEntity {
        val cachedUser = App.instance.getCachedUser()

        return ChatEntity(
            chatId = getWaitingChatId(),
            chatRoomId = roomId,
            senderId = cachedUser.userId,
            senderNickname = cachedUser.userNickname,
            senderProfileImageUrl = cachedUser.userProfileImageUri,
            senderDefaultProfileImageType = cachedUser.defaultProfileImageType,
            dispatchTime = DateManager.getCurrentDateTimeString(),
            status = ChatStatus.LOADING,
            message = message,
            chatType = ChatType.Mine
        )
    }

    private suspend fun getWaitingChatId(): Long {
        val minChatId = database.chatDAO().getMinLoadingChatId()
        return if ((minChatId == null) || (minChatId > 0L)) MIN_CHAT_ID
        else database.chatDAO().getMaxLoadingChatId()?.plus(1) ?: MIN_CHAT_ID
    }

    private fun sendEnterMessage() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { chatRepository.sendEnterChatRoom(stompSession, roomId) }
            .onSuccess { firstEnterFlag = false }
            .onFailure { handleError(it) }
    }

    fun finishActivity() {
        closeSocket()
        startEvent(ChatEvent.MoveBack)
    }

    private fun closeSocket() {
        unSubscribeTopic()
        disconnectSocket()
    }

    private fun unSubscribeTopic() {
        observeChatFlowJob.cancel()
        observeErrorFlowJob.cancel()
    }

    private fun disconnectSocket() = viewModelScope.launch {
        runCatching { stompSession.disconnect() }
            .onFailure { handleError(it) }
    }

    fun clickNewChatNotice() {
        startEvent(ChatEvent.ScrollNewChatItem)
    }

    private fun startEvent(event: ChatEvent) = viewModelScope.launch {
        eventFlow.emit(event)
    }

    //TODO : 예외처리 분기 추가해야함
    private fun handleErrorFlow(errorText: String) {
        Log.d(TAG, "ChatRoomViewModel: handleErrorFlow() - errorText : $errorText")
    }

    //TODO : 예외처리 분기 추가해야함 (대부분이 현재 세션 연결 취소 후 다시 재연결 해야 함)
    private suspend fun handleError(throwable: Throwable) {
        withContext(Dispatchers.Main) {
            when (throwable) {
//            is MissingHeartBeatException -> {}
//            is ConnectionException -> {}
//            is LostReceiptException -> {}
                else -> {
                    Log.d(TAG, "ChatRoomViewModel: handleError() $throwable")
                    makeToast(R.string.error_network_error)
                }
            }
        }
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    sealed class ChatEvent {
        object MoveBack : ChatEvent()
        object CaptureChat : ChatEvent()
        object OpenMenu : ChatEvent()
        object ScrollNewChatItem : ChatEvent()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(
            chatRoomEntity: ChatRoomEntity,
            firstEnterFlag: Boolean
        ): ChatRoomViewModel
    }

    companion object {
        private const val LOCAL_DATA_CHAT_LOAD_SIZE = 25

        fun provideFactory(
            assistedFactory: AssistedFactory,
            chatRoomEntity: ChatRoomEntity,
            firstEnterFlag: Boolean
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(chatRoomEntity, firstEnterFlag) as T
            }
        }
    }
}