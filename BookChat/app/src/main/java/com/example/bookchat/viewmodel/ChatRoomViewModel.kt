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
import com.example.bookchat.data.MessageType.*
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.local.dao.ChatDAO.Companion.MIN_CHAT_ID
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatStatus
import com.example.bookchat.data.local.entity.ChatEntity.ChatType
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.paging.remotemediator.ChatRemoteMediator
import com.example.bookchat.repository.ChatRepository
import com.example.bookchat.repository.ChatRoomManagementRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DateManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.hildan.krossbow.stomp.StompSession
import kotlin.time.Duration.Companion.minutes

// TODO : 비즈니스 로직만 남기고 잡다한 요청은 Repository에 local, remote 구분해서 이전
class ChatRoomViewModel @AssistedInject constructor(
    @Assisted val chatRoomEntity: ChatRoomEntity,
    private val chatRepository: ChatRepository,
    private val chatRoomManagementRepository: ChatRoomManagementRepository
) : ViewModel() {
    val eventFlow = MutableSharedFlow<ChatEvent>()

    val inputtedMessage = MutableStateFlow(chatRoomEntity.tempSavedMessage ?: "")
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
    // 소켓 연결 끊어졌을 때, 전송 대기중인 채팅 있다면 전부 전송 실패로 변경
    // 소켓 재 연결 시마다, 채팅방 정보 조회 API 호출해서 수정사항 전부 덮어쓰기
    // 소켓 재 연결 시마다, 내가 가지고 있던 채팅부터 서버의 마지막 채팅(isLast가 올 때)까지 페이징 요청
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
//        viewModelScope.launch(Dispatchers.IO) {
        viewModelScope.launch {
            chatRoomManagementRepository.getChatRoomInfo(roomId)
            //채팅방 정보 조회 API 호출 (성공 여부 상관없음)
            //소켓 연결 요청
            //소켓 연결 성공시 채팅 Topic 구독
            //소켓 연결 성공시 에러 Topic 구독
            //소켓 연결 성공 /실패 상관 없이 끝나면 채팅방 채팅 내역 조회
            //전송 대기 혹은 전송 실패 채팅 가져오기

            //채팅방 정보 조회 실패시 Queue에서 해당 내용 지우지 않음
            //소켓 연결 실패시 요청 Queue에서 해당 내용 지우지 않음

            //Queue에 있던 내용은 실패 시 Queue의 마지막으로 넣고
            //성공시 빼는 걸로,
            //소켓 연결이 끊기면 소켓 연결이 끊겼을 떄 해야하는 작업들을 Queue에 넣어야 함
            //이거 혹시 Queue가 아니라 WorkManager?? (이거 였으면 좋겠다..)

            //전송 중 상태인 채팅들 전송 실패로 바꾸는건 WorkManager로 하면 될 듯
            clearTempSavedMessage()
            connectSocket()
            observeErrorFlowJob.start()
            observeChatFlowJob.start()
            observeWaitingChatFlowJob.start()
        }
    }

    private suspend fun clearTempSavedMessage() {
        database.withTransaction {
            database.chatRoomDAO().setTempSavedMessage(roomId, "")
        }
    }

    private fun startAutoTokenRenewal() {
        //TODO : 20분 간격으로 토큰 갱신 요청 보내는 로직 추가해야함 (화면이 꺼졌을 떄는 안보내는게 좋을듯) (더 좋은 방법 생각)
        //TODO : 채팅 켜둔채로 화면 꺼졌다가 다시 들어왔을때, 토큰이 만료되어서 채팅 전송이 실패할 수 도 있음 (예외처리)
        //특정 시간 주기로 갱신 요청시
        //  일정 시간(13분)마다 토큰 갱신 요청을 보내야함
        //갱신을 실패하면 어쩔?
        //  Activity의 생명주기가 Stop되었을 때는, 갱신요청을 보내면 안됨
        //  Activity의 생명주기가 Resume이 되면 다시 갱신 요청 보내고 그 시점부터 특정 간격당 계속 보내야함
        // + 해당 Activity 들어오기 전에 혹은 들어오자말자 토큰 갱신부터 하고, 성공 시 소켓 연결 하는걸로 구현해야함함
    }

    //TODO : 전송 대기중에 인터넷이 끊겨도, 인터넷이 연결되는 즉시 전송이 되어야함 (인터넷 연결상태를 observe하고 있어야함)
    //TODO : 요청 실패시 다시 실패했던 요청 재 전송해야함 (실패 이유별로 분기 필요)
    //TODO : 소켓 연결 끊기면 마지막 채팅으로 페이징 한 번 하고 다시 소켓 연결 (특정횟수만큼 소켓 재연결 요청)
    //TODO : 예외별로 예외처리

    private suspend fun connectSocket() {
        runCatching { chatRepository.getStompSession() }
            .onSuccess { stompSession = it }
            .onFailure { handleError(it) }
    }

    private val observeChatFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) {
            runCatching { collectChatTopic() }
                .onFailure { handleError(it) }
        }

    private val observeErrorFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) {
            runCatching { collectErrorTopic() }
                .onFailure { handleError(it) }
        }

    private val observeWaitingChatFlowJob = viewModelScope
        .launch(start = CoroutineStart.LAZY) { collectWaitingChatList() }

    private suspend fun collectChatTopic() {
        subscribeChatTopic(roomSId).collect { socketMessage -> saveChatInLocalDB(socketMessage) }
    }

    private suspend fun collectErrorTopic() {
        subscribeErrorTopic().collect { handleErrorTopic(it) }
    }

    private suspend fun collectWaitingChatList() {
        database.chatDAO().getWaitingChatList(roomId).collect { waitingChatList = it }
    }

    private suspend fun saveChatInLocalDB(socketMessage: SocketMessage) {
        when (socketMessage) {
            is SocketMessage.CommonMessage -> saveCommonMessageInLocalDB(socketMessage)
            is SocketMessage.NotificationMessage -> saveNoticeMessageInLocalDB(socketMessage)
        }
    }

    private suspend fun saveCommonMessageInLocalDB(
        socketMessage: SocketMessage.CommonMessage
    ) {
        val myUserId = App.instance.getCachedUser().userId
        val chatEntity = socketMessage.toChatEntity(roomId)

        database.withTransaction {
            if (socketMessage.senderId != myUserId) {
                if (!isFirstItemOnScreen) newOtherChatNoticeFlow.value = chatEntity
                insertNewChat(chatEntity)
                updateChatRoomLastChatInfo(chatEntity)
                return@withTransaction
            }

            //TODO : 현재 message로 비교하는로직 수정 예정
            // 헤더에 ReceiptID가 추가로 넘어오거나
            // Receipt Frame으로 성공여부 판별되게
            for (waitingChat in waitingChatList) {
                if (waitingChat.message.length != socketMessage.message.length) continue
                if (waitingChat.message != socketMessage.message) continue
                updateChatInfo(chatEntity, waitingChat.chatId)
                break
            }
            updateChatRoomLastChatInfo(chatEntity)
        }
    }

    private suspend fun saveNoticeMessageInLocalDB(
        socketMessage: SocketMessage.NotificationMessage
    ) {
        val chatEntity = socketMessage.toChatEntity(roomId)
        // TODO :Target에 대한 DB수정 작업해야함
        database.withTransaction {
            val noticeTarget = socketMessage.targetId

            when (socketMessage.messageType) {
                CHAT -> {}
                ENTER -> {
                    database.chatRoomDAO().updateMemberCount(roomId, 1)
                }
                EXIT -> {
                    //만약 방장이 나갔다면 채팅방 삭제 후 공지 띄워야함,
                    //채팅방 인원 수 감소
                    database.chatRoomDAO().updateMemberCount(roomId, -1)
                }
                NOTICE_KICK -> {
                    //채팅방 인원 수 감소
                    database.chatRoomDAO().updateMemberCount(roomId, -1)
                }
                NOTICE_HOST_DELEGATE -> {
                    //방장 변경
                }
                NOTICE_SUB_HOST_DISMISS -> {
                    //Target 부방장 해임
                }
                NOTICE_SUB_HOST_DELEGATE -> {
                    //Target 부방장 위임
                }
            }

            if (!isFirstItemOnScreen) newOtherChatNoticeFlow.value = chatEntity
            insertNewChat(chatEntity)
            updateChatRoomLastChatInfo(chatEntity)
            return@withTransaction
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
    // TODO :launch(Dispatchers.IO) 추가 해야할 것 같으면 추가하기
    private suspend fun subscribeChatTopic(roomSid: String): Flow<SocketMessage> {
        runCatching { chatRepository.subscribeChatTopic(stompSession, roomSid) }
            .onSuccess { return it }
        throw Exception("Fail subscribeChatRoom")
    }

    // TODO :launch(Dispatchers.IO) 추가 해야할 것 같으면 추가하기
    private suspend fun subscribeErrorTopic(): Flow<String> {
        runCatching { chatRepository.subscribeErrorTopic(stompSession) }
            .onSuccess { return it }
        throw Exception("Fail subscribeSocketError")
    }

    fun sendMessage() = viewModelScope.launch(Dispatchers.IO) {
        if (inputtedMessage.value.isBlank()) return@launch
        val message = inputtedMessage.value.also { inputtedMessage.value = "" }
        scrollForcedFlag = true
        insertNewChat(getLoadingChatEntity(message))
        runCatching { chatRepository.sendMessage(stompSession, roomId, message) }
            .onSuccess { Log.d(TAG, "ChatRoomViewModel: sendMessage() - 메세지 전송 성공 !! $it") }
            .onFailure { handleError(it) }
    }

    private suspend fun getLoadingChatEntity(message: String): ChatEntity {
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

    fun finishActivity() {
        closeSocket()
        startEvent(ChatEvent.MoveBack)
    }

    private fun closeSocket() = viewModelScope.launch {
        unSubscribeTopic()
        disconnectSocket()
    }

    //TODO : cancel 성공여부 보고 disconnect하기
    private suspend fun unSubscribeTopic() {
        observeChatFlowJob.cancelAndJoin()
        observeErrorFlowJob.cancelAndJoin()
    }

    private suspend fun disconnectSocket() {
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
    private fun handleErrorTopic(errorText: String) {
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
        fun create(chatRoomEntity: ChatRoomEntity): ChatRoomViewModel
    }

    companion object {
        private val TOKEN_RENEW_DURATION = 20.minutes
        private const val LOCAL_DATA_CHAT_LOAD_SIZE = 25

        fun provideFactory(
            assistedFactory: AssistedFactory,
            chatRoomEntity: ChatRoomEntity
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(chatRoomEntity) as T
            }
        }
    }
}