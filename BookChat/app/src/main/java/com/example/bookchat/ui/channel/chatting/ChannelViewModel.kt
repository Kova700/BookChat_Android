package com.example.bookchat.ui.channel.chatting

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.domain.NetworkManager
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.NetworkState
import com.example.bookchat.domain.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.StompHandler
import com.example.bookchat.domain.usecase.GetChatsFlowUseCase
import com.example.bookchat.domain.usecase.SyncChannelChatsUseCase
import com.example.bookchat.ui.channel.chatting.ChannelUiState.LoadState
import com.example.bookchat.ui.channel.chatting.ChannelUiState.UiState
import com.example.bookchat.ui.channel.chatting.mapper.toChatItems
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.ui.channel.drawer.mapper.toDrawerItems
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
//TODO : 장문의 긴 채팅 길이 접기 구현해야함, 누르면 전체보기 가능하게
//TODO : 채팅 꾹 누르면 복사
//TODO : 채팅방 정보 조회 실패 시 예외 처리
//TODO : 채팅 로딩 전체 화면 UI 구현
//TODO : 카톡처럼 이모지 한개이면 이모지 크기 확대
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
	private val clientRepository: ClientRepository,
	private val networkManager: NetworkManager,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelEvent>()
	val eventFlow get() = _eventFlow

	private val _uiState = MutableStateFlow<ChannelUiState>(ChannelUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	//TODO :
	// 6. Notice타입의 NewChatNotice UI

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val originalChannel = channelRepository.getChannel(channelId)

		val shouldLastReadChatScroll = originalChannel.isExistNewChat
		updateState {
			copy(
				channel = originalChannel,
				client = clientRepository.getClientProfile(),
				originalLastReadChatId = originalChannel.lastReadChatId,
				isVisibleLastReadChatNotice = shouldLastReadChatScroll,
				needToScrollToLastReadChat = shouldLastReadChatScroll,
			)
		}

		if (originalChannel.isAvailableChannel.not()) return@launch

		getChannelInfo(channelId)
		getTempSavedMessage(channelId)
		observeChannel()
		observeChats()
		getOfflineNewestChats()
		observeChatsLoadState()
		observeSocketState()
		observeNetworkState()
	}

	private fun observeNetworkState() = viewModelScope.launch {
		networkManager.getStateFlow().collect { state ->
			updateState { copy(networkState = state) }
			when (state) {
				NetworkState.CONNECTED -> connectSocket("observeNetworkState")
				NetworkState.DISCONNECTED -> Unit
			}
		}
	}

	private suspend fun getOfflineNewestChats() {
		chatRepository.getOfflineNewestChats(channelId)
	}

	private fun observeChannel() = viewModelScope.launch {
		channelRepository.getChannelFlow(channelId).collect { channel ->
			if (channel.isAvailableChannel.not()) disconnectSocket()
			handleChannelNewChat(channel)
			updateState {
				copy(
					channel = channel,
					drawerItems = channel.toDrawerItems(client)
				)
			}
		}
	}

	private fun handleChannelNewChat(channel: Channel) {
		val channelLastChat = channel.lastChat ?: return
		if (channel.isExistNewChat.not()) return

		when (channelLastChat.chatType) {
			ChatType.Mine -> scrollToBottom()
			ChatType.Notice, //TODO : Notice타입의 NewChatNotice UI 만들기
			ChatType.Other,
			-> startEvent(ChannelEvent.NewChatOccurEvent(channelLastChat))
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
				channel = uiState.value.channel,
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
			SocketState.NEED_RECONNECTION -> connectSocket("handleSocketState")
		}
	}

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
			}.onFailure { handleError(it, "getNewestChats") } //TODO : api 실패 알림
			.getOrNull()
	}

	private fun getNewerChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadNewerChat.not()) return@launch
		updateState { copy(newerChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getNewerChats(channelId) }
			.onSuccess { updateState { copy(newerChatsLoadState = LoadState.SUCCESS) } }
			.onFailure { handleError(it, "getNewerChats") } //TODO : api 실패 알림
	}

	private fun getOlderChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadOlderChat.not()) return@launch
		updateState { copy(olderChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getOlderChats(channelId) }
			.onSuccess { updateState { copy(olderChatsLoadState = LoadState.SUCCESS) } }
			.onFailure { handleError(it, "getOlderChats") } //TODO : api 실패 알림
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
			.onFailure { handleError(it, "getChatsAroundId") } //TODO : 그냥 로컬데이터 최하단 화면 유지
	}

	private fun onChannelConnectFail() = viewModelScope.launch {
		updateState { copy(uiState = UiState.ERROR) }
		startEvent(ChannelEvent.MakeToast(R.string.error_socket_connect))
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트 간의 데이터 불일치를 메우기 위해서 임시로
	 * 리커넥션 시마다 호출 (이벤트 History받는 로직 구현 이전까지 )*/
	private fun getChannelInfo(channelId: Long) = viewModelScope.launch {
		if (uiState.value.isNetworkDisconnected) return@launch
		runCatching { channelRepository.getChannelInfo(channelId) }
			.onFailure { handleError(it, "getChannelInfo") }
	}

	private fun connectSocket(caller: String) = viewModelScope.launch {
		if (uiState.value.isNetworkDisconnected) return@launch
		Log.d(TAG, "ChannelViewModel: connectSocket() - caller : $caller")
		val channel = channelRepository.getChannel(channelId)
		runCatching { stompHandler.connectSocket(channel) }
			.onFailure { handleError(it, "connectSocket") }
	}

	private fun sendMessage(text: String? = null) {
		if (uiState.value.channel.isAvailableChannel.not()) return
		if (uiState.value.socketState != SocketState.CONNECTED) connectSocket("sendMessage")

		val message = text ?: uiState.value.enteredMessage
		if (message.isBlank()) return

		updateState { copy(enteredMessage = "") }
		clearTempSavedMessage()

		viewModelScope.launch {
			runCatching {
				stompHandler.sendMessage(
					channelId = channelId,
					message = message,
				)
			}
				.onFailure {
					handleError(it, "sendMessage")
				} //TODO : 소켓 닫혔을 때, 메세지 보내면 StompErrorFrameReceived 넘어옴
		}
	}

	private fun deleteFailedChat(chatId: Long) = viewModelScope.launch {
		chatRepository.deleteChat(chatId)
	}

	private fun retryFailedChat(chatId: Long) = viewModelScope.launch {
		if (uiState.value.socketState != SocketState.CONNECTED) return@launch
		stompHandler.retrySendMessage(chatId)
	}

	private fun exitChannel() = viewModelScope.launch {
		runCatching { channelRepository.leaveChannel(channelId) }
			.onSuccess { onClickBackBtn() }
			.onFailure { startEvent(ChannelEvent.MakeToast(R.string.channel_exit_fail)) }
	}

	/** "여기까지 읽으셨습니다" 공지가 화면 상에 나타나는 순간 호출*/
	fun onReadLastReadNotice() {
		if (uiState.value.needToScrollToLastReadChat.not()
			|| uiState.value.socketState != SocketState.CONNECTED
		) return
		updateState { copy(needToScrollToLastReadChat = false) }
	}

	private fun disconnectSocket() = viewModelScope.launch {
		Log.d(TAG, "ChannelViewModel: disconnectSocket() - called")
		stompHandler.disconnectSocket()
	}

	fun onStartScreen() {
		if (uiState.value.channel.isAvailableChannel.not()) return
		connectSocket("onStartScreen")
	}

	fun onStopScreen() {
		if (uiState.value.channel.isAvailableChannel.not()) return
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
		runCatching { syncChannelChatsUseCase(channelId) }
			.onFailure { handleError(it, "syncChats") }
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
	fun onChangeStateOfLookingAtBottom(isBottom: Boolean) {
		val isLookingAtBottom = isBottom && uiState.value.isNewerChatFullyLoaded
		if (uiState.value.isLookingAtBottom == isLookingAtBottom) return
		updateState { copy(isLookingAtBottom = isLookingAtBottom) }
	}

	/** 리스트 상 내 채팅이 아닌 채팅 중 가장 최신 채팅이 화면 상에 나타나는 순간 호출 */
	fun onReadNewestChatNotMineInList(chatItem: ChatItem.Message) {
		val nowNewChatNotice = uiState.value.newChatNotice
		if (chatItem.chatId != nowNewChatNotice?.chatId) return
		updateState { copy(newChatNotice = null) }
	}

	fun onClickScrollToBottom() {
		scrollToBottom()
	}

	fun onClickNewChatNotice() {
		scrollToBottom()
	}

	//TODO : 캡처 기능 추가
	fun onClickCaptureBtn() {
		if (uiState.value.channel.isAvailableChannel.not()) return
//		startEvent(ChannelEvent.CaptureChannel)
	}

	fun onClickMenuBtn() {
		startEvent(ChannelEvent.OpenOrCloseDrawer)
	}

	fun onClickChannelExitBtn() {
		startEvent(ChannelEvent.ShowChannelExitWarningDialog(uiState.value.clientAuthority))
	}

	fun onClickChannelExitDialogBtn() {
		exitChannel()
	}

	fun onClickChannelSettingBtn() {
		startEvent(ChannelEvent.MoveChannelSetting)
	}

	fun onClickUserProfile(user: User) {
		startEvent(ChannelEvent.MoveUserProfile(user))
	}

	fun onClickFailedChatDeleteBtn(chatId: Long) {
		deleteFailedChat(chatId)
	}

	fun onClickFailedChatRetryBtn(chatId: Long) {
		retryFailedChat(chatId)
	}

	private fun startEvent(event: ChannelEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	//TODO : 예외처리 분기 추가해야함 (대부분이 현재 세션 연결 취소 후 다시 재연결 해야 함)
	private fun handleError(throwable: Throwable, caller: String = "기본") {
		Log.d(TAG, "ChannelViewModel: handleError(caller : $caller) - throwable: $throwable")
		updateState { copy(uiState = UiState.ERROR) }
		when (throwable) {
			is CancellationException -> Unit //JobCancellationException 이게 connectSocket 여기서 왜 잡히는거야
			//NullPointerException도 connectSocket에서 잡히는데?
			//ChannelViewModel: handleError(caller : connectSocket) -
			// throwable: java.lang.NullPointerException: Parameter specified as non-null is null:
			// method com.example.bookchat.domain.model.Chat.<init>, parameter dispatchTime
			else -> startEvent(ChannelEvent.MakeToast(R.string.error_network_error))
		}
	}

	private inline fun updateState(block: ChannelUiState.() -> ChannelUiState) {
		_uiState.update { _uiState.value.block() }
	}
}