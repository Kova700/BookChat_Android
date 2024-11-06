package com.kova700.bookchat.core.chatclient

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.kova700.bookchat.core.chat_client.R
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.core.stomp.chatting.external.model.ChannelSubscriptionFailureException
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketConnectionFailureException
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.external.model.SubscriptionState
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.bookchat.util.toast.makeToast
import com.kova700.core.domain.usecase.client.LogoutUseCase
import com.kova700.core.domain.usecase.client.WithdrawUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//TODO : [Version 2] 소켓 관리 방식 수정
//        기존 구조 : 채팅방 입장시 소켓 연결 + 해당 채팅방 구독 (구독되지 않은 채팅방은 FCM을 통해 메세지를 받는 구조_
//				기존 구조의 문제점 : 간혹 FCM이 매우 늦게 수신되는 상황이 발생함
//        수정된 구조 : 소켓 연결 + 페이징된 채널 모두 구독 (현재 구조상 실시간성을 만족하기 위한 최선의 방법)
//        수정될 구조 : Stomp가 아닌 WebSocket + 커스텀 프로토콜 정의해서 사용
//        이유 : 모든 채널에 대한 이벤트를 웹소켓으로 받기로 정한 후부터
//        존재하는 모든 채널에 대한 구독을 할 것임으로 굳이 클라이언트에서
//        모든 채널에 대한 구독 요청을 보내는 상황이 불필요하고 비효율적이게됨
//TODO : [Version 2] 동기화 방식 수정
//        기존 구조 : 소켓 재연결시 현재 보고 있는 채팅방에 한하여 getChannelInfo와 채팅방의 현재 마지막 채팅부터 채팅 내역 페이징 요청
//        수정될 구조 : 페이징 되어있는 채팅방에 한하여 서버에게 ChattingHistory를 API로 가져오는 방식으로 수정

class ChatClientImpl @Inject constructor(
	@ApplicationContext private val appContext: Context,
	private val networkManager: NetworkManager,
	private val stompHandler: StompHandler,
	private val clientRepository: ClientRepository,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val logoutUseCase: LogoutUseCase,
	private val withdrawUseCase: WithdrawUseCase,
) : ChatClient {

	private val chatClientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	init {
		observeNetworkState()
		observeSocketState()
		observeChannelList()
	}

	override fun isChannelSubscribed(channelId: Long): Boolean =
		stompHandler.isChannelSubscribed(channelId)

	override fun getSocketStateFlow(): Flow<SocketState> =
		stompHandler.getSocketStateFlow()

	override fun getChannelSubscriptionStateFlow(channelId: Long): Flow<SubscriptionState> =
		stompHandler.getChannelSubscriptionStateFlow(channelId)

	override fun connectSocketIfNeeded() {
		if (stompHandler.isSocketConnected.not()) connectSocket()
	}

	private val isSocketConnected
		get() = stompHandler.isSocketConnected

	/** 특정 채널에 입장했을때, 구독이 안되어있을 경우 사용 */
	override fun subscribeChannelIfNeeded(channelId: Long) = chatClientScope.launch {
		if (isSocketConnected.not()) connectSocket().join()
		if (isChannelSubscribed(channelId).not()) subscribeChannel(
			channelId = channelId,
			shouldShowToast = true
		).join()
	}

	override fun sendMessage(channelId: Long, message: String) {
		chatClientScope.launch {
			subscribeChannelIfNeeded(channelId).join()
			stompHandler.sendMessage(channelId, message)
		}
	}

	override fun retrySendMessage(chatId: Long) {
		chatClientScope.launch {
			val chat = chatRepository.getChat(chatId) ?: return@launch
			subscribeChannelIfNeeded(chat.channelId).join()
			stompHandler.retrySendMessage(chatId)
		}
	}

	override suspend fun logout() {
		disconnectSocket()
		logoutUseCase()
	}

	override suspend fun withdraw() {
		disconnectSocket()
		withdrawUseCase()
	}

	private fun connectSocket() = chatClientScope.launch {
		if (clientRepository.isClientLoggedIn().not()) return@launch
		runCatching { stompHandler.connectSocket() }
			.onSuccess { subscribeAllChannels() }
			.onFailure { throwable ->
				if (throwable is SocketConnectionFailureException) {
					showToast(R.string.error_socket_connect)
				}
			}
	}

	private fun subscribeAllChannels() = chatClientScope.launch {
		if (isSocketConnected.not()) return@launch
		val loadedChannelIds = channelRepository.getChannelsFlow()
			.firstOrNull()?.map { it.roomId } ?: return@launch
		subscribeChannels(loadedChannelIds)
	}

	private fun subscribeChannels(channelIds: List<Long>) {
		channelIds.forEach { channelId ->
			if (isSocketConnected.not()) return
			subscribeChannel(channelId)
		}
	}

	private fun subscribeChannel(
		channelId: Long,
		shouldShowToast: Boolean = false
	) = chatClientScope.launch {
		if (isSocketConnected.not()) return@launch
		val channel = channelRepository.getChannel(channelId) ?: return@launch
		if (channel.isAvailable.not()) return@launch
		runCatching { stompHandler.subscribeChannel(channelId) }
			.onSuccess { messageFlow ->
				chatClientScope.launch { messageFlow.collect {} }
				retryChannelFailedChats(channelId)
			}
			.onFailure { throwable ->
				if (throwable is ChannelSubscriptionFailureException && shouldShowToast) {
					showToast(appContext.getString(R.string.error_channel_subscribe, channel.roomName))
				}
			}
	}

	/** 소켓이 끊긴 사이에 발생한 전송 실패 상태 채팅들을 구독 성공 시 일괄 재전송 */
	private suspend fun retryChannelFailedChats(channelId: Long) {
		chatRepository.getFailedChats(channelId)
			.filter { it.state == ChatState.RETRY_REQUIRED }
			.reversed()
			.forEach { chat -> retrySendMessage(chat.chatId) }
	}

	private fun disconnectSocket() = chatClientScope.launch {
		Log.d(TAG, "ChatClientImpl: disconnectSocket() - called")
		stompHandler.disconnectSocket()
	}

	private fun observeChannelList() = chatClientScope.launch {
		channelRepository.getChannelsFlow()
			.map { channels -> channels.map { it.roomId } }
			.distinctUntilChanged()
			.collect { channelIds -> subscribeChannels(channelIds) }
	}

	private fun observeNetworkState() = chatClientScope.launch {
		networkManager.observeNetworkState().collect { state ->
			when (state) {
				NetworkState.CONNECTED -> connectSocket()
				NetworkState.DISCONNECTED -> Unit
			}
		}
	}

	private fun observeSocketState() = chatClientScope.launch {
		stompHandler.getSocketStateFlow()
			.filter { it == SocketState.NEED_RECONNECTION }
			.collect { connectSocket() }
	}

	override fun onResume(owner: LifecycleOwner) {
		super.onResume(owner)
		connectSocket()
	}

	override fun onPause(owner: LifecycleOwner) {
		super.onPause(owner)
		disconnectSocket()
	}

	override fun onDestroy(owner: LifecycleOwner) {
		super.onDestroy(owner)
		chatClientScope.cancel()
	}

	private suspend fun showToast(messageId: Int) {
		withContext(Dispatchers.Main) { appContext.makeToast(messageId) }
	}

	private suspend fun showToast(message: String) {
		withContext(Dispatchers.Main) { appContext.makeToast(message) }
	}

}