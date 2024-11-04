package com.kova700.bookchat.feature.channel.chatting

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.chatclient.ChannelSyncManger
import com.kova700.bookchat.core.chatclient.ChatClient
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.repository.ChannelTempMessageRepository
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatType
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.notification.chat.external.ChatNotificationHandler
import com.kova700.bookchat.core.remoteconfig.RemoteConfigManager
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.external.model.SubscriptionState
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity.Companion.EXTRA_CHANNEL_ID
import com.kova700.bookchat.feature.channel.chatting.ChannelUiState.LoadState
import com.kova700.bookchat.feature.channel.chatting.ChannelUiState.UiState
import com.kova700.bookchat.feature.channel.chatting.mapper.toChatItems
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem
import com.kova700.bookchat.feature.channel.drawer.mapper.toDrawerItems
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.core.domain.usecase.channel.GetClientChannelFlowUseCase
import com.kova700.core.domain.usecase.channel.GetClientChannelInfoUseCase
import com.kova700.core.domain.usecase.channel.GetClientChannelUseCase
import com.kova700.core.domain.usecase.channel.LeaveChannelUseCase
import com.kova700.core.domain.usecase.chat.GetChannelChatsFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

// TODO : [FixWaiting] 유저 프로필 변경 시 drawerItem에 바로 반영안됨 수정 필요
// TODO : [FixWaiting] 채팅방 화면에서 홀드 버튼 누르거나 화면 꺼지면 Notification이 안뜨는 현상있음
// TODO : [FixWaiting] 소켓 끊김 사이에 발생한 수정사항 동기화 테스트 필요함
// TODO : [FixWaiting] ChannelMember 리스트가 텅빈채로 보이는 현상이 있음

@HiltViewModel
class ChannelViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
	private val chatClient: ChatClient,
	private val getClientChannelInfoUseCase: GetClientChannelInfoUseCase,
	private val getClientChannelUseCase: GetClientChannelUseCase,
	private val getClientChannelFlowUseCase: GetClientChannelFlowUseCase,
	private val getChatsFlowUserCase: GetChannelChatsFlowUseCase,
	private val leaveChannelUseCase: LeaveChannelUseCase,
	private val channelTempMessageRepository: ChannelTempMessageRepository,
	private val chatRepository: ChatRepository,
	private val clientRepository: ClientRepository,
	private val networkManager: NetworkManager,
	private val chatNotificationHandler: ChatNotificationHandler,
	private val remoteConfigManager: RemoteConfigManager,
	private val channelSyncManger: ChannelSyncManger,
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

	init {
		initUiState()
	}

	private fun initUiState() = viewModelScope.launch {
		Log.d(TAG, "ChannelViewModel: initUiState() - channelId: $channelId")
		if (isBookChatAvailable().not()) return@launch
		val originalChannel = getClientChannelUseCase(channelId) ?: return@launch
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
		observeChats()
		getOfflineNewestChats()
		getChannelInfo(channelId)
		if (originalChannel.isAvailable.not()) return@launch
		getTempSavedMessage(channelId)
		observeChannel()
		observeChatsLoadState()
		observeSocketState()
		observeChannelSubscriptionState()
	}

	private suspend fun getOfflineNewestChats() {
		Log.d(TAG, "ChannelViewModel: getOfflineNewestChats(channelId : $channelId) - called")
		chatRepository.getOfflineNewestChats(channelId)
	}

	private fun observeChannel() = viewModelScope.launch {
		getClientChannelFlowUseCase(channelId)
			.onEach { chatNotificationHandler.dismissChannelNotifications(it) }
			.collect { channel ->
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

		when (channelLastChat.getChatType(uiState.value.client.id)) {
			ChatType.Mine -> scrollToBottom()
			ChatType.Notice,
			ChatType.Other -> startEvent(ChannelEvent.NewChatOccurEvent(channelLastChat))
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
			captureIds,
			uiState.map { it.isVisibleLastReadChatNotice }.distinctUntilChanged()
		) { chats, captureHeaderBottomIds, isVisibleLastReadChatNotice ->
			chats.toChatItems(
				channel = uiState.value.channel,
				clientId = uiState.value.client.id,
				captureHeaderItemId = captureHeaderBottomIds?.first,
				captureBottomItemId = captureHeaderBottomIds?.second,
				focusTargetId = uiState.value.originalLastReadChatId,
				isVisibleLastReadChatNotice = isVisibleLastReadChatNotice
			)
		}.collect { chats -> updateState { copy(chats = chats) } }
	}

	private fun observeChatsLoadState() {
		viewModelScope.launch {
			chatRepository.getOlderChatIsEndFlow().collect { isFullyLoad ->
				updateState { copy(isOlderChatFullyLoaded = isFullyLoad) }
			}
		}
		viewModelScope.launch {
			chatRepository.getNewerChatIsEndFlow().collect { isFullyLoad ->
				updateState { copy(isNewerChatFullyLoaded = isFullyLoad) }
			}
		}
	}

	private fun observeSocketState() = viewModelScope.launch {
		chatClient.getSocketStateFlow().collect { state ->
			updateState { copy(socketState = state) }
		}
	}

	private fun observeChannelSubscriptionState() = viewModelScope.launch {
		var isFirstFailed = true
		chatClient.getChannelSubscriptionStateFlow(channelId).collect { state ->
			Log.d(TAG, "ChannelViewModel: observeChannelSubscriptionState() - state : $state")
			when (state) {
				SubscriptionState.SUBSCRIBING -> Unit
				SubscriptionState.UNSUBSCRIBED -> subscribeChannelIfNeeded()
				SubscriptionState.FAILED -> {
					if (isFirstFailed.not()) return@collect
					subscribeChannelIfNeeded()
					isFirstFailed = false
				}

				SubscriptionState.SUBSCRIBED -> {
					when {
						uiState.value.isFirstConnection.not() -> syncChannelIfNeeded()
						else -> {
							getInitChats()
							updateState { copy(isFirstConnection = false) }
						}
					}
				}
			}
		}
	}

	//TODO : [Version 2] 현재 보고 있는 채팅방에 한해서만 동기화 요청되게 사용하고 있지만,
	// 			추후 EventHistory를 통해 모든 채팅방에 대한 동기화 요청으로 수정하고 ChatClient로 이전
	private fun syncChannelIfNeeded() {
		Log.d(TAG, "ChannelViewModel: syncChannelsIfNeeded() - called")
		runCatching { channelSyncManger.sync(listOf(channelId)) }
			.onFailure { ChannelEvent.ShowSnackBar(R.string.channel_info_sync_fail) }
	}

	private suspend fun isBookChatAvailable(): Boolean {
		val remoteConfigValues = getRemoteConfig().await() ?: return true
		when {
			remoteConfigValues.isServerEnabled.not() -> {
				startEvent(
					ChannelEvent.ShowServerDisabledDialog(
						message = remoteConfigValues.serverDownNoticeMessage
					)
				)
				return false
			}

			remoteConfigValues.isServerUnderMaintenance -> {
				startEvent(
					ChannelEvent.ShowServerMaintenanceDialog(
						message = remoteConfigValues.serverUnderMaintenanceNoticeMessage
					)
				)
				return false
			}
		}
		return true
	}

	private fun getRemoteConfig() = viewModelScope.async {
		runCatching { remoteConfigManager.getRemoteConfig() }.getOrNull()
	}

	/** 마지막으로 읽었던 채팅이 getNewestChats 결과에 포함되어있다면 getChatsAroundId는 호출하지 않음 */
	private fun getInitChats() = viewModelScope.launch {
		if (networkManager.isNetworkAvailable().not()
			|| uiState.value.channel.isAvailable.not()
		) return@launch
		Log.d(TAG, "ChannelViewModel: getInitChats(channelId : $channelId) - called")
		val newestChats = getNewestChats().await() ?: emptyList()
		val originalLastReadChatId = uiState.value.originalLastReadChatId ?: return@launch
		val shouldLastReadChatScroll =
			originalLastReadChatId < (newestChats.firstOrNull()?.chatId ?: -1)
		if (shouldLastReadChatScroll) {
			updateState {
				copy(
					isVisibleLastReadChatNotice = true,
					needToScrollToLastReadChat = true,
				)
			}
		}
		val shouldCallGetChatsAroundId =
			newestChats.any { it.chatId == originalLastReadChatId }.not()
		if (shouldCallGetChatsAroundId) getChatsAroundId(originalLastReadChatId)
	}

	private fun getNewestChats(shouldBottomScroll: Boolean = false) = viewModelScope.async {
		Log.d(TAG, "ChannelViewModel: getNewestChats() - called")
		if (uiState.value.isInitLoading) return@async null
		Log.d(TAG, "ChannelViewModel: getNewestChats() - 실제 API 호출")
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching { chatRepository.getNewestChats(channelId) }
			.onSuccess {
				updateState {
					copy(
						uiState = UiState.SUCCESS,
						newChatNotice = null
					)
				}
				if (shouldBottomScroll) startEvent(ChannelEvent.ScrollToBottom)
			}.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
			}.getOrNull()
	}

	private fun getNewerChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadNewerChat.not()) return@launch
		Log.d(TAG, "ChannelViewModel: getNewerChats() - called")
		updateState { copy(newerChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getNewerChats(channelId) }
			.onSuccess { updateState { copy(newerChatsLoadState = LoadState.SUCCESS) } }
			.onFailure {
				updateState { copy(newerChatsLoadState = LoadState.ERROR) }
				startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
			}
	}

	private fun getOlderChats() = viewModelScope.launch {
		if (uiState.value.isPossibleToLoadOlderChat.not()) return@launch
		Log.d(TAG, "ChannelViewModel: getOlderChats() - called")
		updateState { copy(olderChatsLoadState = LoadState.LOADING) }
		runCatching { chatRepository.getOlderChats(channelId) }
			.onSuccess { updateState { copy(olderChatsLoadState = LoadState.SUCCESS) } }
			.onFailure {
				updateState { copy(olderChatsLoadState = LoadState.ERROR) }
				startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
			}
	}

	private fun getChatsAroundId(baseChatId: Long) = viewModelScope.launch {
		if (uiState.value.isInitLoading) return@launch
		updateState { copy(uiState = UiState.INIT_LOADING) }
		runCatching {
			chatRepository.getChatsAroundId(
				channelId = channelId,
				baseChatId = baseChatId
			)
		}.onSuccess { updateState { copy(uiState = UiState.SUCCESS) } }
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
			}
	}

	/** 소켓이 끊긴 사이에 발생한 서버와 클라이언트 간의 데이터 불일치를 메우기 위해서
	 * 리커넥션 시마다 호출 (이벤트 History받는 로직 구현 이전까지 임시로 사용)*/
	private fun getChannelInfo(channelId: Long) = viewModelScope.launch {
		if (networkManager.isNetworkAvailable().not()
			|| uiState.value.channel.isAvailable.not()
		) return@launch
		runCatching { getClientChannelInfoUseCase(channelId) }
			.onFailure {
				updateState { copy(uiState = UiState.ERROR) }
				startEvent(ChannelEvent.ShowSnackBar(R.string.error_network_error))
			}
	}

	private fun subscribeChannelIfNeeded() {
		chatClient.subscribeChannelIfNeeded(channelId)
	}

	private fun sendMessage() {
		if (uiState.value.channel.isAvailable.not()) return
		val message = uiState.value.enteredMessage
		if (message.isBlank()) return
		updateState { copy(enteredMessage = "") }
		clearTempSavedMessage()
		chatClient.sendMessage(
			channelId = channelId,
			message = message,
		)
	}

	private fun deleteFailedChat(chatId: Long) = viewModelScope.launch {
		chatRepository.deleteChat(chatId)
	}

	private fun retryFailedChat(chatId: Long) {
		if (uiState.value.channel.isAvailable.not()) return
		chatClient.retrySendMessage(chatId)
	}

	private fun exitChannel() = viewModelScope.launch {
		runCatching { leaveChannelUseCase(channelId) }
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

	private fun scrollToBottom() {
		when {
			uiState.value.isNewerChatFullyLoaded -> startEvent(ChannelEvent.ScrollToBottom)
			else -> getNewestChats(true)
		}
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
		if (text.length > MAX_CHAT_MESSAGE_LENGTH) return
		updateState { copy(enteredMessage = text) }
		saveTempSavedMessage(text.trim())
	}

	fun onClickSendMessageBtn() {
		if (uiState.value.isCaptureMode) return
		sendMessage()
	}

	fun onClickBackBtn() {
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

	//TODO : [FixWaiting] getAroundId로 채팅 중앙에 있다가 아래로 내리는 과정에 새로운 채팅이 생기면 아래에 NewChatNotice가 생김
	// 그 상황에 새로운 채팅을 Load하기 위해서 채팅 하단으로 스크롤을 내리면 채팅 최하단에 왔다고 인식하고 Noti를 지우는 상황이 생겨버림
	// 테스트 해보고 아래 각주가 필요하다면 추가 하기
	/** 리스트 상 내 채팅이 아닌 채팅 중 가장 최신 채팅이 화면 상에 나타나는 순간 호출 */
	fun onReadNewestChatNotMineInList(chatItem: ChatItem.Message) {
		val nowNewChatNotice = uiState.value.newChatNotice ?: return
		if (chatItem.chatId < nowNewChatNotice.chatId) return
//		if (uiState.value.isNewerChatFullyLoaded.not()) return
		updateState { copy(newChatNotice = null) }
	}

	fun onClickScrollToBottomBtn() {
		scrollToBottom()
	}

	fun onClickNewChatNotice() {
		scrollToBottom()
	}

	fun onClickCaptureBtn() {
		if (uiState.value.channel.isAvailable.not()
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
		startEvent(ChannelEvent.ShowChannelExitWarningDialog(uiState.value.isClientHost))
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
		if (uiState.value.isCaptureMode.not()) return
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

	fun onClickMoveToWholeText(chatId: Long) {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.MoveToWholeText(chatId))
	}

	fun onClickFailedChatRetryBtn(chatId: Long) {
		if (uiState.value.isCaptureMode) return
		retryFailedChat(chatId)
	}

	fun onLongClickChatItem(message: String) {
		if (uiState.value.isCaptureMode) return
		startEvent(ChannelEvent.CopyChatToClipboard(message))
	}

	fun onCopiedToClipboard() {
		startEvent(ChannelEvent.ShowSnackBar(R.string.channel_chatting_copied_to_clipboard))
	}

	private fun startEvent(event: ChannelEvent) = viewModelScope.launch {
		eventFlow.emit(event)
	}

	private inline fun updateState(block: ChannelUiState.() -> ChannelUiState) {
		_uiState.update { _uiState.value.block() }
	}

	companion object {
		const val MAX_CHAT_MESSAGE_LENGTH = 10000
	}
}