package com.kova700.bookchat.core.chatclient

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.kova700.bookchat.core.chat_client.R
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
	private val logoutUseCase: LogoutUseCase,
	private val withdrawUseCase: WithdrawUseCase
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

	/** 특정 채널에 입장했을때, 구독이 안되어있을 경우 사용 */
	override fun subscribeChannelIfNeeded(
		channelId: Long,
		channelSId: String
	) {
		chatClientScope.launch {
			if (stompHandler.isSocketConnected.not()) connectSocket().join()
			stompHandler.subscribeChannel(channelId, channelSId)
		}
	}

	//TODO : [FixWaiting] 소켓이 연결된 채로 채팅이 왔는데,
	// 아래 알림만 나오고 아래 새로운 채팅을 로드 하지 않는 현상이 있음
	// + 아래 채팅 공지를 눌러야 로드하고 있음
	override fun sendMessage(channelId: Long, message: String) {
		chatClientScope.launch {
			stompHandler.sendMessage(channelId, message)
		}
	}

	//TODO : [FixWaiting] 채팅 전송중 생명주기 ClientScope로 만들기 (ChannelActivity에서 채팅 전송중에 화면을 나가도 전송중인 채팅 취소되지 않게)
	override fun retrySendMessage(chatId: Long) {
		chatClientScope.launch {
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

	//요구사항
	// 1. 소켓이 끊기면 자동으로 재연결되어야함                                                (O)
	// 2. 클라이언트가 로그인 되어있지 않다면(본인 프로필을 가지고 있지 않다면) 소켓이 연결되어선 안됨   (O)
	// 3. 소켓 재연결된다면, 현재 로드되어있는 모든 채팅방에 대한 구독을 자동으로 해야함               (O)
	// 4. 채팅방이 추가로 로드된다면 구독도 추가로 자동으로 되어야함                               (O)
	// 5. 소켓 재연결 시 구독함수를 호출해도 상관은 없으나 중복 재구독만큼은 되어선 안됨               (O)
	// etc. 사실상 스톰프에서 모든 채널을 구독해야 일반 소켓처럼 사용가능함으로
	// 소켓 연결과 모든 채널 구독은 하나의 트랜젝션으로 수행되어야함 (그냥 StompHandler에서 소켓 연결이랑 전부 묶어서 하자)
	// 그 채널 중 하나의 채널이라도 구독을 실패한다면..?
	// (실패한 놈은 그냥 일단 두자, 되면 이득 안되면 말고 식) + 채팅방 들어가면 자동으로 다시 구독요청되게
	private fun connectSocket() = chatClientScope.launch {
		if (clientRepository.isClientLoggedIn().not()) return@launch
		runCatching { stompHandler.connectSocket() }
			.onSuccess { subscribeAllChannels() }
			.onFailure { appContext.makeToast(R.string.error_socket_connect) }
	}

	private fun disconnectSocket() = chatClientScope.launch {
		Log.d(TAG, "ChatClientImpl: disconnectSocket() - called")
		stompHandler.disconnectSocket()
	}

	private fun subscribeAllChannels() = chatClientScope.launch {
		if (stompHandler.isSocketConnected.not()) return@launch
		val loadedChannels = channelRepository.getChannelsFlow()
			.firstOrNull() ?: return@launch
		loadedChannels.forEach { channel ->
			if (stompHandler.isSocketConnected.not()) return@launch
			chatClientScope.launch {
				stompHandler.subscribeChannel(channel.roomId, channel.roomSid)
			}
		}
	}

	private fun observeChannelList() = chatClientScope.launch {
		channelRepository.getChannelsFlow()
			.map { channels -> channels.map { it.roomId to it.roomSid } }
			.distinctUntilChanged()
			.collect {
				it.forEach { (channelId, channelSId) ->
					if (stompHandler.isSocketConnected.not()) return@forEach
					chatClientScope.launch {
						stompHandler.subscribeChannel(channelId, channelSId)
					}
				}
			}
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
		stompHandler.getSocketStateFlow().collect(::handleSocketState)
	}

	private fun handleSocketState(state: SocketState) {
		if (state == SocketState.NEED_RECONNECTION) connectSocket()
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

}