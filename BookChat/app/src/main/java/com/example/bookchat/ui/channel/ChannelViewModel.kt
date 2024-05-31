package com.example.bookchat.ui.channel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.StompHandler
import com.example.bookchat.domain.usecase.GetChatsFlowUseCase
import com.example.bookchat.domain.usecase.SyncChannelChatsUseCase
import com.example.bookchat.ui.channel.ChannelUiState.LoadState
import com.example.bookchat.ui.channel.ChannelUiState.UiState
import com.example.bookchat.ui.channel.mapper.chat.toChatItems
import com.example.bookchat.ui.channel.mapper.drawer.toDrawerItems
import com.example.bookchat.ui.channel.model.chat.ChatItem
import com.example.bookchat.ui.channelList.ChannelListFragment.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.utils.Constants.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 점검 중 , 새로운 업데이트 RemoteConfig 구성해서 출시 + Crashtics
//TODO : Activity단위로 소켓 연결이 아닌 앱단위 소켓연결로 수정해서 FCM누락시 정확도 향상 기대
//TODO : 장문의 긴 채팅 길이 접기 구현해야함, 누르면 전체보기 가능하게
//TODO : 채팅 꾹 누르면 복사
//TODO : 뒤로가기 누를 때, 혹시 채팅방 메뉴 켜져 있으면 닫고, 화면 안꺼지게 수정
//TODO : 채팅방에 들어왔을 때, 채팅스크롤의 위치는 내가 마지막으로 받았던 채팅 (여기까지 읽었습니다)
//TODO : 전송 중 상태로 어플 종료 시 전송실패로 변경된 UI로 보이게 수정
//TODO : 또한 전송 중 상태에서 인터넷이 끊겨도, 다시 인터넷이 연결되면 자동으로 전송이 되어야 함
//TODO : 전송 대기 중이던 채팅 소켓 연결 끊길 시, 혹은 특정 시간 지나면, 전송 실패 UI로 전환
//TODO : 소켓 재 연결 시마다, 채팅방 정보 조회 API 호출해서 수정 사항 전부 덮어쓰기
//TODO : 소켓 재 연결 시마다, 로컬에 저장된 마지막 채팅부터 서버의 마지막 채팅(isLast가 올 때)까지 페이징 요청
//TODO : 소켓 연결 실패 시 예외 처리
//TODO : 채팅방 정보 조회 실패 시 예외 처리
//TODO : 소켓 연결 성공 /실패 상관 없이 끝나면 채팅방 채팅 내역 조회
//TODO : Network 연결 상태 Flow로 실시간 알림 받는 환경 구성
//TODO : channel isExplode 상태면 채팅창 입력 비활성화 UI 구현
//TODO : 임시로 NewChat있다고 파란 동그라미 표시라도 하는게 좋을 듯 (해도 되긴 할듯)
//        (새로운 기기로 채팅방을 불러왔는데 채팅은 계속 추가되고 있음 + 읽은 채팅정보가 로컬에 없으면 N+ 표시없이 채팅이 갱신될거임)
//TODO : 채팅 로딩 전체 화면 UI 구현
//TODO : 인터넷 재연결되면 소켓도 재연결 로직
//TODO : 카톡처럼 이모지 한개이면 이모지 크기 확대
//TODO : 채팅 간격이 너무 넓음 수정
//TODO : 출시 전 북챗 문의 방 만들기

@HiltViewModel
class ChannelViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val tempMessageDAO: TempMessageDAO, //개선 필요(+ 레이어 구분도 필요)
	private val stompHandler: StompHandler,
	private val getChatsFlowUserCase: GetChatsFlowUseCase,
	private val syncChannelChatsUseCase: SyncChannelChatsUseCase,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelEvent>()
	val eventFlow get() = _eventFlow

	private val _uiState = MutableStateFlow<ChannelUiState>(ChannelUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	//TODO :
	// 3. 인터넷 재연결되면 소켓 재연결 트리거
	// 4. 소켓 재연결되면 RETRY_REQUIRED인 애들은 일괄 전송
	// 5. 유저 리스트 , 채팅 리스트 방장 부방장 권한 반영 + 현재 권한에 따른 UI 구분
	// 6. Notice타입의 NewChatNotice UI
	// 7. Bottom 이동 버튼 UI

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val originalChannel = channelRepository.getChannel(channelId)
		val shouldLastReadChatScroll = when {
			originalChannel.lastReadChatId == null -> false
			originalChannel.lastChat?.chatId == null -> false
			originalChannel.lastReadChatId < originalChannel.lastChat.chatId -> true
			else -> false
		}
		updateState {
			copy(
				channel = originalChannel,
				originalLastReadChatId = originalChannel.lastReadChatId,
				isVisibleLastReadChatNotice = shouldLastReadChatScroll,
				needToScrollToLastReadChat = shouldLastReadChatScroll,
			)
		}

		getChannelInfo(channelId)
		getTempSavedMessage(channelId)
		observeChannel()
		observeChats()
		getOfflineNewestChats()
		observeChatsLoadState()
		observeSocketState()
		connectSocket(channelId)
	}

	private suspend fun getOfflineNewestChats() {
		chatRepository.getOfflineNewestChats(channelId)
	}

	private fun observeChannel() = viewModelScope.launch {
		channelRepository.getChannelFlow(channelId).collect { channel ->
			handleChannelNewChat(channel)
			updateState {
				copy(
					channel = channel,
					drawerItems = channel.toDrawerItems() //TODO : 방장 부방장 권한 반영해야함
				)
			}
		}
	}

	private fun handleChannelNewChat(channel: Channel) {
		val channelLastChat = channel.lastChat
		val channelLastReadChatId = channel.lastReadChatId

		if ((channelLastChat == null || channelLastReadChatId == null) ||
			(channelLastChat.chatId <= channelLastReadChatId)
		) return

		when (channelLastChat.chatType) {
			ChatType.Mine -> scrollToBottom()
			ChatType.Notice, //TODO : Notice타입의 NewChatNotice UI 만들기
			ChatType.Other -> startEvent(ChannelEvent.NewChatOccurEvent(channelLastChat))
		}
	}

	fun onNeedNewChatNotice(channelLastChat: Chat) {
		updateState { copy(newChatNotice = channelLastChat) }
	}

	private fun observeChats() = viewModelScope.launch {
		getChatsFlowUserCase(
			initFlag = true,
			channelId = channelId
		).map { chats ->
			chats.toChatItems(
				focusTargetId = uiState.value.originalLastReadChatId,
				isVisibleLastReadChatNotice = uiState.value.isVisibleLastReadChatNotice
			)
		}.collect { chats -> updateState { copy(chats = chats) } }
	}

	private fun observeChatsLoadState() {
		viewModelScope.launch {
			chatRepository.getOlderChatIsEndFlow().collect { isFullyLoad ->
				Log.d(
					TAG,
					"ChannelViewModel: observeChatsLoadState() - isOlderChatFullyLoaded : $isFullyLoad"
				)
				updateState { copy(isOlderChatFullyLoaded = isFullyLoad) }
			}
		}
		viewModelScope.launch {
			chatRepository.getNewerChatIsEndFlow().collect { isFullyLoad ->
				Log.d(
					TAG,
					"ChannelViewModel: observeChatsLoadState() - isNewerChatFullyLoaded : $isFullyLoad"
				)
				updateState { copy(isNewerChatFullyLoaded = isFullyLoad) }
			}
		}
	}

	private fun observeSocketState() = viewModelScope.launch {
		stompHandler.getSocketStateFlow().collect { state -> handleSocketState(state) }
	}

	private fun handleSocketState(state: SocketState) {
		Log.d(TAG, "ChannelViewModel: handleSocketState() - state :$state")
		updateState { copy(socketState = state) }
		when (state) {
			SocketState.DISCONNECTED -> Unit
			SocketState.CONNECTING -> Unit
			SocketState.CONNECTED -> onChannelConnected()
			SocketState.FAILURE -> onChannelConnectFail()
			SocketState.NEED_RECONNECTION -> onReconnection()
		}
	}

	//TODO : 소켓 리커넥션 혹은 연결 성공후 채팅 가져올 때,
	// RETRY_REQUIRED인 애들은 일괄 전송,
	// LOADING인 애들은 FAILURE처리 (waiting시 소켓 끊긴 경우 재전송으로 인한 중복 전송 방지)
	private fun onChannelConnected() {
		if (uiState.value.isFirstConnection) {
			getFirstChats()
			updateState { copy(isFirstConnection = false) }
		} else {
			syncChannelState()
		}
	}

	/** 스크롤 해야할 채팅이 getNewestChats 결과에 포함되어있다면 getChatsAroundId는 호출하지 않음 */
	private fun getFirstChats() = viewModelScope.launch {
		Log.d(TAG, "ChannelViewModel: getFirstChats() - called")
		val newestChats = getNewestChats().await() ?: emptyList()
		if (uiState.value.needToScrollToLastReadChat.not()) return@launch
		val originalLastReadChatId = uiState.value.originalLastReadChatId ?: return@launch
		val shouldCallGetChatsAroundId =
			newestChats.map(Chat::chatId).contains(originalLastReadChatId).not()
		if (shouldCallGetChatsAroundId) getChatsAroundId(originalLastReadChatId)
	}

	private fun getNewestChats(shouldBottomScroll: Boolean = false) = viewModelScope.async {
		if (uiState.value.uiState == UiState.LOADING) return@async null
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chatRepository.getNewestChats(channelId) }
			.onSuccess {
				updateState {
					copy(
						uiState = UiState.SUCCESS,
						newChatNotice = null
					)
				}
				if (shouldBottomScroll) startEvent(ChannelEvent.ScrollToBottom)
			}.onFailure { handleError(it) } //TODO : api 실패 알림
			.getOrNull()
	}

	//TODO : 상단에 프로그래스바 추가
	private fun getNewerChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadNewerChat.not()) return@launch
		updateState { copy(newerChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getNewerChats(channelId) }
			.onSuccess { updateState { copy(newerChatsLoadState = LoadState.SUCCESS) } }
			.onFailure { handleError(it) } //TODO : api 실패 알림
	}

	//TODO : 하단에 프로그래스바 추가
	private fun getOlderChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadOlderChat.not()) return@launch
		updateState { copy(olderChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getOlderChats(channelId) }
			.onSuccess { updateState { copy(olderChatsLoadState = LoadState.SUCCESS) } }
			.onFailure { handleError(it) } //TODO : api 실패 알림
	}

	private fun getChatsAroundId(baseChatId: Long) = viewModelScope.launch {
		if (uiState.value.uiState == UiState.LOADING) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching {
			chatRepository.getChatsAroundId(
				channelId = channelId,
				baseChatId = baseChatId
			)
		}
			.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure { handleError(it) } //TODO : 그냥 로컬데이터 최하단 화면 유지
	}

	private fun onChannelConnectFail() = viewModelScope.launch {
		updateState { copy(uiState = UiState.ERROR) }
		startEvent(ChannelEvent.MakeToast(R.string.error_socket_connect))
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트 간의 데이터 불일치를 메우기 위해서 임시로
	 * 리커넥션 시마다 호출 (이벤트 History받는 로직 구현 이전까지 )*/
	private fun getChannelInfo(channelId: Long) = viewModelScope.launch {
		runCatching { channelRepository.getChannelInfo(channelId) }
			.onFailure { handleError(it) }
	}

	private fun connectSocket(channelId: Long) = viewModelScope.launch {
		val channel = channelRepository.getChannel(channelId)
		if (checkChannelConnectable(channel).not()) return@launch
		runCatching { stompHandler.connectSocket(channel) }
			.onFailure { handleError(it, "connectSocket") }
	}

	private fun sendMessage() {
		val message = uiState.value.enteredMessage
		if (message.isBlank()) return

		updateState { copy(enteredMessage = "") }
		clearTempSavedMessage()
		viewModelScope.launch {
			runCatching {
				stompHandler.sendMessage(
					channelId = channelId,
					message = message
				)
			}
				.onFailure {
					handleError(it, "sendMessage")
				} //소켓 닫혔을 때, 메세지 보내면 StompErrorFrameReceived 넘어옴
		}
	}

	/** "여기까지 읽으셨습니다" 공지가 화면 상에 나타나는 순간 호출*/
	fun onReadLastReadNotice() {
		if (uiState.value.needToScrollToLastReadChat.not()
			|| uiState.value.socketState != SocketState.CONNECTED
		) return
		updateState { copy(needToScrollToLastReadChat = false) }
	}

	private fun disconnectSocket() = viewModelScope.launch {
		stompHandler.disconnectSocket()
	}

	fun onStartScreen() {
		onReconnection()
	}

	fun onStopScreen() {
		disconnectSocket()
		updateState { copy(socketState = SocketState.DISCONNECTED) }
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트간의 채널 데이터 불일치 동기화*/
	private fun syncChannelState() {
		getChannelInfo(channelId)
		syncChats(channelId)
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트간의 채팅 불일치 동기화 */
	private fun syncChats(channelId: Long) = viewModelScope.launch {
		syncChannelChatsUseCase(channelId)
	}

	//TODO : 인터넷(WIFI, Data)연결되면 해당 함수 trigger되어야함
	// (인터넷 끊겨있으면 작동 x (안막으면 지수 백오프 돌아감) , 시작점도 이렇게 해야하려나..? )
	/** 리커넥션이 필요할때 호출되는 함수 */
	private fun onReconnection() {
		connectSocket(channelId)
	}

	private fun checkChannelConnectable(channel: Channel): Boolean {
		return when {
			channel.isBanned -> {
				onBanned()
				false
			}

			channel.isExploded -> {
				onExplodeChannel()
				false
			}

			else -> true
		}
	}

	private fun onBanned() {
		//TODO : "채팅방 관리자에의해 강퇴되었습니다." Notice Dialog
		//  isBanned로 변경된 채팅방을 emit받은 ChannelActivity에서는 채팅 입력을 불가능하게 비활성화 UI를 노출
	}

	private fun onExplodeChannel() {
		//TODO : "방장이 채팅방을 종료했습니다. 더 이상 대화가 불가능합니다." Notice Dialog
		//  isExplode로 변경된 채팅방을 emit받은 ChannelActivity에서는 채팅 입력을 불가능하게 비활성화 UI를 노출
	}

	private fun scrollToBottom() {
		if (uiState.value.isNewerChatFullyLoaded) {
			startEvent(ChannelEvent.ScrollToBottom)
			return
		}

		getNewestChats(true)
	}

	private fun getTempSavedMessage(channelId: Long) = viewModelScope.launch {
		runCatching { tempMessageDAO.getTempMessage(channelId)?.message }
			.onSuccess {
				updateState { copy(enteredMessage = it ?: "") }
			}
	}

	private fun saveTempSavedMessage(text: String) = viewModelScope.launch {
		if (text.isBlank()) return@launch
		tempMessageDAO.insertOrUpdateTempMessage(channelId, text)
	}

	private fun clearTempSavedMessage() = viewModelScope.launch {
		tempMessageDAO.setTempSavedMessage(channelId, "")
	}

	fun onChangeEnteredMessage(text: String) {
		updateState { copy(enteredMessage = text) }
		saveTempSavedMessage(text.trim())
	}

	fun onClickSendMessage() {
		sendMessage()
	}

	fun onClickBackBtn() {
		disconnectSocket()
		startEvent(ChannelEvent.MoveBack)
	}

	/** 주어진 아이템 끝에 도달 */
	fun onReachedTopChat() {
		getOlderChats()
	}

	/** 주어진 아이템 바닥에 도달 */
	fun onReachedBottomChat() {
		getNewerChats()
	}

	//TODO : 홀릭스 / 카톡 버튼처럼 애니메이션으로 만들어보자
	// 아래에 newChatNotice있을 땐 띄우지 않음 + 없을 때 스크롤 발생하면 띄우기
	fun onChangeStateOfLookingAtBottom(isBottom: Boolean) {
		//viewmodel업데이트  isLookingAtBottom
		//isLookingAtBottom == true라면 아래로 가기 버튼 invisible
		//isLookingAtBottom == false라면 아래로 가기 버튼 visible
	}

	/** 리스트 상 내 채팅이 아닌 채팅 중 가장 최신 채팅이 화면 상에 나타나는 순간 호출 */
	fun onReadNewestChatNotMineInList(chatItem: ChatItem.Message) {
		val nowNewChatNotice = uiState.value.newChatNotice
		if (chatItem.chatId != nowNewChatNotice?.chatId) return
		updateState { copy(newChatNotice = null) }
	}

	//TODO : UI 추가
	private fun onClickScrollToBottom() {
		scrollToBottom()
	}

	fun onClickNewChatNotice() {
		scrollToBottom()
	}

	fun onClickMenuBtn() {
		startEvent(ChannelEvent.OpenOrCloseDrawer)
	}

	fun onClickUserProfile(user: User) {
		startEvent(ChannelEvent.MoveUserProfile(user))
	}

	private fun startEvent(event: ChannelEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	//TODO : 예외처리 분기 추가해야함 (대부분이 현재 세션 연결 취소 후 다시 재연결 해야 함)
	private suspend fun handleError(throwable: Throwable, caller: String = "기본") {
		Log.d(TAG, "ChannelViewModel: handleError(caller : $caller) - throwable: $throwable")
		updateState { copy(uiState = UiState.ERROR) }
		when (throwable) {
			is CancellationException -> Unit //JobCancellationException 이게 connectSocket 여기서 왜 잡히는거야
			else -> startEvent(ChannelEvent.MakeToast(R.string.error_network_error))
		}
	}

	private inline fun updateState(block: ChannelUiState.() -> ChannelUiState) {
		_uiState.update { _uiState.value.block() }
	}
}