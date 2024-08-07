package com.example.bookchat.ui.channel.chatting

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.networkmanager.external.NetworkManager
import com.example.bookchat.data.networkmanager.external.model.NetworkState
import com.example.bookchat.data.stomp.external.StompHandler
import com.example.bookchat.data.stomp.external.model.SocketState
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChannelTempMessageRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.usecase.GetChatsFlowUseCase
import com.example.bookchat.domain.usecase.SyncChannelChatsUseCase
import com.example.bookchat.notification.chat.ChatNotificationHandler
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

//TODO : 점검 중 , 새로운 업데이트 RemoteConfig 구성해서 출시 + Crashtics
//TODO : 장문의 긴 채팅 길이 접기 구현해야함, 누르면 전체보기 가능하게
//TODO : 채팅 꾹 누르면 복사
//TODO : 채팅방 정보 조회 실패 시 예외 처리
//TODO : 채팅 로딩 전체 화면 UI 구현
//TODO : 카톡처럼 이모지 한 개이면 이모지 크기 확대
//TODO : 출시 전 북챗 문의 방 만들기

@HiltViewModel
class ChannelViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val stompHandler: StompHandler,
	private val getChatsFlowUserCase: GetChatsFlowUseCase,
	private val syncChannelChatsUseCase: SyncChannelChatsUseCase,
	private val channelTempMessageRepository: ChannelTempMessageRepository,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val clientRepository: ClientRepository,
	private val networkManager: NetworkManager,
	private val chatNotificationHandler: ChatNotificationHandler,
) : ViewModel() {
	private val channelId = savedStateHandle.get<Long>(EXTRA_CHANNEL_ID)!!

	private val _eventFlow = MutableSharedFlow<ChannelEvent>()
	val eventFlow get() = _eventFlow

	private val _uiState = MutableStateFlow<ChannelUiState>(ChannelUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	/** 너무 잦은 chatItem 갱신을 방지하기 위해 uiState와 분리하여 combine*/
	private val _captureIds =
		MutableStateFlow<Pair<Long, Long>?>(null) // (headerId,bottomId)
	val captureIds get() = _captureIds.asStateFlow()

	//TODO :
	// 6. Notice타입의 NewChatNotice UI

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		val originalChannel = runCatching { channelRepository.getChannel(channelId) }
			.getOrNull() ?: Channel.DEFAULT

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
		observeNetworkState()
		//TODO: 채널에서 강퇴되었는지 , 채널이 폭파되었는지 알기 위해 해당 API를 호출해야함
		// 하지만,isBaned, isExploded가 넘어오는게 아니라 강퇴 당하면 그냥 404 {"errorCode":"4040500","message":"참여자를 찾을 수 없습니다."} 넘어옴
		// 채팅방 터진 경우는 404안뜨고 isExploded = true로 잘 넘어오긴함 (서버 수정 대기중)
		getChannelInfo(channelId)
		if (originalChannel.isAvailableChannel.not()) return@launch
		getTempSavedMessage(channelId)
		observeChannel()
		observeChats()
		getOfflineNewestChats()
		observeChatsLoadState()
		observeSocketState()
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
		channelRepository.getChannelFlow(channelId)
			.onEach { chatNotificationHandler.dismissChannelNotifications(it) }
			.collect { channel ->
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
		combine(
			getChatsFlowUserCase(
				initFlag = true,
				channelId = channelId
			),
			captureIds
		) { chats, captureHeaderBottomIds ->
			chats.toChatItems(
				channel = uiState.value.channel,
				captureHeaderItemId = captureHeaderBottomIds?.first,
				captureBottomItemId = captureHeaderBottomIds?.second,
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
			newestChats.any { it.chatId == originalLastReadChatId }.not()
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
		startEvent(ChannelEvent.ShowSnackBar(R.string.error_socket_connect))
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트 간의 데이터 불일치를 메우기 위해서 임시로
	 * 리커넥션 시마다 호출 (이벤트 History받는 로직 구현 이전까지 )*/
	private fun getChannelInfo(channelId: Long) = viewModelScope.launch {
//		if (uiState.value.isNetworkDisconnected) return@launch
		runCatching { channelRepository.getChannelInfo(channelId) }
			.onFailure { handleError(it, "getChannelInfo") }
	}

	private fun connectSocket(caller: String) = viewModelScope.launch {
		if (uiState.value.isNetworkDisconnected) return@launch
		Log.d(TAG, "ChannelViewModel: connectSocket() - caller : $caller")
		if (uiState.value.channel == Channel.DEFAULT) return@launch
		runCatching { stompHandler.connectSocket(uiState.value.channel) }
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
			.onFailure { startEvent(ChannelEvent.ShowSnackBar(R.string.channel_exit_fail)) }
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
		channelTempMessageRepository.getTempMessage(channelId)?.let { message ->
			updateState { copy(enteredMessage = message) }
		}
	}

	private fun saveTempSavedMessage(text: String) = viewModelScope.launch {
		if (text.isBlank()) return@launch
		channelTempMessageRepository.saveTempMessage(
			channelId = channelId,
			message = text
		)
	}

	private fun clearTempSavedMessage() = viewModelScope.launch {
		channelTempMessageRepository.deleteTempMessage(channelId)
	}

	fun onChangeEnteredMessage(text: String) {
		updateState { copy(enteredMessage = text) }
		saveTempSavedMessage(text.trim())
	}

	fun onClickSendMessage() {
		if (uiState.value.isCaptureMode) return
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

	fun onClickCaptureBtn() {
		if (uiState.value.channel.isAvailableChannel.not()
			|| uiState.value.chats.isEmpty()
		) return
		updateState { copy(isCaptureMode = true) }
		_captureIds.update { null }
	}

	fun onClickMenuBtn() {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.OpenOrCloseDrawer)
	}

	fun onClickChannelExitBtn() {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.ShowChannelExitWarningDialog(uiState.value.clientAuthority))
	}

	fun onClickCancelCaptureSelection() {
		_captureIds.update { null }
	}

	fun onClickCancelCapture() {
		updateState { copy(isCaptureMode = false) }
		_captureIds.update { null }
	}

	fun onFailedCapture() {
		startEvent(ChannelEvent.ShowSnackBar(R.string.channel_scrap_fail))
	}

	fun onCompletedCapture() {
		onClickCancelCapture()
		startEvent(ChannelEvent.ShowSnackBar(R.string.channel_scrap_success))
	}

	fun onClickCompleteCapture() {
		val (headerId, bottomId) = captureIds.value ?: return
		val chatMessages = uiState.value.chats
		val headerIndex = chatMessages.indexOfFirst { it.getCategoryId() == headerId }
		val bottomIndex = chatMessages.indexOfFirst { it.getCategoryId() == bottomId }
		if (headerIndex == -1 || bottomIndex == -1) return

		startEvent(
			ChannelEvent.MakeCaptureImage(
				headerIndex = headerIndex,
				bottomIndex = bottomIndex
			)
		)
	}

	fun onSelectCaptureChat(chatItemId: Long) {
		if (uiState.value.isCaptureMode.not()) return

		val (headerId, bottomId) = captureIds.value ?: Pair(null, null)
		val chatMessages = uiState.value.chats
		var newHeaderId = headerId
		var newBottomId = bottomId

		when {
			headerId == null || bottomId == null -> {
				newHeaderId = chatItemId
				newBottomId = chatItemId
			}

			headerId == bottomId -> {
				val newSelectedIndex = chatMessages.indexOfFirst { it.getCategoryId() == chatItemId }
				val headerIndex = chatMessages.indexOfFirst { it.getCategoryId() == headerId }
				val currentIndex = headerIndex
				when {
					currentIndex < newSelectedIndex -> newHeaderId = chatItemId
					currentIndex > newSelectedIndex -> newBottomId = chatItemId
				}
			}

			headerId != bottomId -> {
				val newSelectedIndex = chatMessages.indexOfFirst { it.getCategoryId() == chatItemId }
				val headerIndex = chatMessages.indexOfFirst { it.getCategoryId() == headerId }
				val bottomIndex = chatMessages.indexOfFirst { it.getCategoryId() == bottomId }
				val headerGap = abs(headerIndex - newSelectedIndex)
				val bottomGap = abs(bottomIndex - newSelectedIndex)
				when {
					headerGap < bottomGap -> newHeaderId = chatItemId
					headerGap > bottomGap -> newBottomId = chatItemId
					else -> newBottomId = chatItemId
				}
			}
		}
		val newHeaderIndex = chatMessages.indexOfFirst { it.getCategoryId() == newHeaderId }
		val newBottomIndex = chatMessages.indexOfFirst { it.getCategoryId() == newBottomId }
		val selectedItemsCount = abs(newHeaderIndex - newBottomIndex) + 1

		if (selectedItemsCount > 30) {
			startEvent(ChannelEvent.ShowSnackBar(R.string.channel_scrap_selected_count_over))
			return
		}
		if (newHeaderId == null || newBottomId == null) _captureIds.update { null }
		else _captureIds.update { Pair(newHeaderId, newBottomId) }
	}

	fun onClickChannelExitDialogBtn() {
		if (uiState.value.isCaptureMode) return
		exitChannel()
	}

	fun onClickChannelSettingBtn() {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.MoveChannelSetting)
	}

	fun onClickUserProfile(user: User) {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.MoveUserProfile(user))
	}

	fun onClickFailedChatDeleteBtn(chatId: Long) {
		if (uiState.value.isCaptureMode) return
		deleteFailedChat(chatId)
	}

	fun onClickFailedChatRetryBtn(chatId: Long) {
		if (uiState.value.isCaptureMode) return
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
			else -> startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
		}
	}

	private inline fun updateState(block: ChannelUiState.() -> ChannelUiState) {
		_uiState.update { _uiState.value.block() }
	}
}