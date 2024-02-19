package com.example.bookchat.viewmodel

import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.withTransaction
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.MessageType.CHAT
import com.example.bookchat.data.MessageType.ENTER
import com.example.bookchat.data.MessageType.EXIT
import com.example.bookchat.data.MessageType.NOTICE_HOST_DELEGATE
import com.example.bookchat.data.MessageType.NOTICE_KICK
import com.example.bookchat.data.MessageType.NOTICE_SUB_HOST_DELEGATE
import com.example.bookchat.data.MessageType.NOTICE_SUB_HOST_DISMISS
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatStatus
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.data.local.entity.ChatWithUser
import com.example.bookchat.paging.remotemediator.ChatRemoteMediator
import com.example.bookchat.repository.ChatRepository
import com.example.bookchat.repository.ChatRoomManagementRepository
import com.example.bookchat.ui.fragment.ChatRoomListFragment.Companion.EXTRA_CHAT_ROOM_LIST_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompSession
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

// TODO : 비즈니스 로직만 남기고 잡다한 요청은 Repository에 local, remote 구분해서 이전
@HiltViewModel
class ChatRoomViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val chatRepository: ChatRepository,
	private val chatRoomManagementRepository: ChatRoomManagementRepository
) : ViewModel() {

	private val database = App.instance.database
	val initChatRoomEntity: ChatRoomEntity =
		savedStateHandle.get<ChatRoomEntity>(EXTRA_CHAT_ROOM_LIST_ITEM)
			?: throw Exception("ChatRoom Information Does Not Exist")
	private val roomId = initChatRoomEntity.roomId
	private val roomSId = initChatRoomEntity.roomSid

	val eventFlow = MutableSharedFlow<ChatEvent>()

	val chatRoomInfoFlow = database.chatRoomDAO().getChatRoom(roomId)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = initChatRoomEntity
		)

	val chatRoomUserListFlow = chatRoomInfoFlow.map { chatRoomEntity ->
		database.userDAO().getUserList(getChatRoomUserIds(chatRoomEntity))
	}

	// TODO : message 객체 구성 변경 receiptID 추가
	//  전체 채팅방 조회시 채팅방 정원 추가됨

	val inputtedMessage = MutableStateFlow("")
	private lateinit var stompSession: StompSession

	var newOtherChatNoticeFlow = MutableStateFlow<ChatWithUser?>(null)
	var isFirstItemOnScreen = true
	var scrollForcedFlag = false

	//TODO : 카톡은 소켓 통신의 생명 주기를 앱의 생명주기를 따르는 듯함
	// 데이터를 꺼두고, 채팅방 목록으로 돌아오고, 다시 데이터를 연결하면
	// 자동으로 채팅이 전송됨

	//TODO : 전송 대기 중이던 채팅 전송 실패 UI로 전환 구현해야함
	// 소켓 연결 끊어졌을 때, 해당 채팅방에 전송 대기중인 채팅 있다면 전부 전송 실패로 변경
	// 우리 서비스는 무거운 채팅을 보내지 않음으로 특정시간이 지나면 전송 실패로 간주하는게 좋을 듯,
	// .
	// 소켓 재 연결 시마다, 채팅방 정보 조회 API 호출해서 수정사항 전부 덮어쓰기
	// 소켓 재 연결 시마다, 내가 가지고 있던 채팅부터 서버의 마지막 채팅(isLast가 올 때)까지 페이징 요청

	//채팅이 입력될 때,마다 25개씩 load해오는게 맞나? 좀 비효울 적인데,,?

	//굳이 RemoteMediator를 쓸 필요 없이.
	//지금 현재 DB상 해당 채팅방의 마지막 채팅부터 ASC형식으로 채팅 요청을 한다.
	// isLast가 넘어오면 끝, 넘어오지 않으면 페이징으로 서버로부터 데이터 가져오기(DESC)
	// 서버로부터 isLast가 올 때까지, 계속 페이징해서 가져오면 될 듯,

	//RemoteMediator가 페이징해서 서버로부터 데이터를 가져오는 기준이 뭘까?
	//1. 무지성으로 일단 한 번 로드한다.
	//2. pagingSource로 부터 데이터를 계속 load를 한다.
	//3. pagingSource로 부터 로드된 데이터가 더이상 없을 때,
	//  pagingSource로부터 로드된 마지막 아이템(state.lastItemOrNull())의 ID로
	//  Remote서버로 부터 새로운 아이템을 로드를 한다.

	//모바일 기술의 네트워크가 여러 번 변경될 수 있고 상태 변경을 모니터링하고
	// 필요한 경우 기존 연결을 해제하고 다시 만들어야 한다는 사실을 수용해야 합니다.

	@OptIn(ExperimentalPagingApi::class)
	val chatDataFlow = Pager(
		config = PagingConfig(pageSize = LOCAL_DATA_CHAT_LOAD_SIZE),
		remoteMediator = ChatRemoteMediator(
			database = database,
			chatRoomId = roomId,
			apiClient = App.instance.bookChatApiClient
		),
		pagingSourceFactory = { database.chatDAO().getChatWithUserPagingSource(roomId) }
	).flow.distinctUntilChanged()

	init {
//        viewModelScope.launch(Dispatchers.IO) {
		viewModelScope.launch {
			getTempSavedMessage()
			requestChatRoomInfo()
			//채팅방 정보 조회 API 호출 (성공 여부 상관없음)
			//소켓 연결 요청
			//소켓 연결 성공시 채팅 Topic 구독
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
			observeChatFlowJob.start()
		}
	}

	private suspend fun requestChatRoomInfo() {
		runCatching { chatRoomManagementRepository.getChatRoomInfo(roomId) }
	}

	private suspend fun getTempSavedMessage() {
		inputtedMessage.value = database.tempMessageDAO().getTempMessage(roomId)?.message ?: ""
	}

	fun saveTempSavedMessage() = viewModelScope.launch {
		val tempMessage = inputtedMessage.value
		if (tempMessage.isBlank()) return@launch
		database.withTransaction {
			database.tempMessageDAO().insertOrUpdateTempMessage(roomId, tempMessage)
		}
	}

	private suspend fun clearTempSavedMessage() {
		database.withTransaction {
			database.tempMessageDAO().setTempSavedMessage(roomId, "")
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

	private suspend fun collectChatTopic() {
		subscribeChatTopic(roomSId).collect { socketMessage -> saveChatInLocalDB(socketMessage) }
	}

	private fun getChatRoomUserIds(chatRoomEntity: ChatRoomEntity): List<Long> {
		return mutableListOf<Long>().apply {
			chatRoomEntity.hostId?.let { add(it) }
			chatRoomEntity.subHostIds?.let { addAll(it) }
			chatRoomEntity.guestIds?.let { addAll(it) }
		}
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
		val receiptId = socketMessage.receiptId
		val chatEntity = socketMessage.toChatEntity(roomId)

		database.withTransaction {
			if (socketMessage.senderId != myUserId) {
				if (!isFirstItemOnScreen) {
					newOtherChatNoticeFlow.value = getChatWithUser(chatEntity)
				}
				insertNewChat(chatEntity)
				updateChatRoomLastChatInfo(chatEntity)
				return@withTransaction
			}
			updateChatInfo(chatEntity, receiptId)
			updateChatRoomLastChatInfo(chatEntity)
		}
	}

	private suspend fun getChatWithUser(chatEntity: ChatEntity): ChatWithUser {
		val sender = database.userDAO().getUser(chatEntity.senderId!!)
		return ChatWithUser(user = sender, chat = chatEntity)
	}

	private suspend fun saveNoticeMessageInLocalDB(
		socketMessage: SocketMessage.NotificationMessage
	) {
		val chatEntity = socketMessage.toChatEntity(roomId)
		// TODO :Target에 대한 DB수정 작업해야함
		database.withTransaction {
			val noticeTarget = socketMessage.targetId

			//TODO : ++ ㅁㅁ님이 입장하셨습니다.
			// ㅁㅁ님이 퇴장하셨습니다.
			// 같이 공지채팅에서 유저이름을 언급하는 경우
			// 실제 닉네임이 아니라 UserId로 주는게 더 좋아보임
			// 문제는 없나..?
			// 메모장에서 언급했던 오래 전 채팅에서 보이는 유저 정보가
			// 최신 정보임을 보장하지 못하는 내용 (그 사람이 현재 채팅방에 없다면)
			// 닉네임, 프로필 사진 등
			// 하지만 카톡도 이렇게 보이는 현상이 있음
			// 이거 결론 짓고, 전달하면 될 듯

			when (socketMessage.messageType) {
				//TODO : 채팅방 터짐(방장 나감)도 추가해야함 + Notice이름 좀 바꾸자.
				CHAT -> {}
				ENTER -> {
					//TODO : 채팅방 정보 조회를 다시하거나 (당첨)
					// Enter NOTICE 속에 유저 정보(채팅방 정보조회의 유저 객체처럼)가
					// 있어야할 것같음 [Id, nickname, profileUrl, defaultImageType]

					//TODO : ㅁㅁ님이 입장 하셨습니다.
					// 이것도 일반 텍스트가 아닌, UserID님이 입장하셨습니다로 보내고
					// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 입장하셨습니다로 변경하는게 좋을 듯
					database.chatRoomDAO().updateMemberCount(roomId, 1)
				}

				EXIT -> {
					//TODO : 채팅방 퇴장 시에 서버에서 넣어주는 채팅 ㅁㅁ님이 퇴장 하셨습니다.
					// 이걸 일반 텍스트가 아닌, UserId님이 퇴장하셨습니다로 보내고
					// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 퇴장하셨습니다로 변경하는게 좋을 듯

					//TODO : 만약 방장이 나갔다면 채팅방 삭제 후 공지 띄우고 채팅 입력 막아야함.
					// 채팅방 인원 수 감소
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

			//TODO : 터질 확률이 높음
			// 유저채팅이 아니고, 프로필도 없고, defaultImageType도 없음으로로
			// if (!isFirstItemOnScreen) newOtherChatNoticeFlow.value = chatEntity
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

	fun sendMessage() = viewModelScope.launch(Dispatchers.IO) {
		if (inputtedMessage.value.isBlank()) return@launch
		val message = inputtedMessage.value.also { inputtedMessage.value = "" }
		scrollForcedFlag = true
		val waitingChatId = insertWaitingChat(roomId, message)
		runCatching { chatRepository.sendMessage(stompSession, roomId, waitingChatId, message) }
			.onFailure { handleError(it) }
	}

	private suspend fun insertWaitingChat(roomId: Long, message: String): Long {
		return database.chatDAO().insertWaitingChat(roomId, message)
	}

	// TODO : Distroy 될 때도 소켓 닫게 수정
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
	}

	private suspend fun disconnectSocket() {
		runCatching { stompSession.disconnect() }
			.onFailure { handleError(it) }
	}

	fun clickNewChatNotice() {
		startEvent(ChatEvent.ScrollNewChatItem)
	}

	fun clickMenuBtn() {
		startEvent(ChatEvent.OpenOrCloseDrawer)
	}

	private fun startEvent(event: ChatEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	//TODO : 예외처리 분기 추가해야함 (대부분이 현재 세션 연결 취소 후 다시 재연결 해야 함)
	private suspend fun handleError(throwable: Throwable) {
		withContext(Dispatchers.Main) {
			when (throwable) {
//            is MissingHeartBeatException -> {}
//            is ConnectionException -> {}
//            is LostReceiptException -> {}
				else -> {
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
		object ScrollNewChatItem : ChatEvent()
		object OpenOrCloseDrawer : ChatEvent()
	}

	companion object {
		private val TOKEN_RENEW_DURATION = 20.minutes
		private const val LOCAL_DATA_CHAT_LOAD_SIZE = 25
		private const val LOCAL_DATA_CHAT_INIT_LOAD_SIZE = 70
	}
}