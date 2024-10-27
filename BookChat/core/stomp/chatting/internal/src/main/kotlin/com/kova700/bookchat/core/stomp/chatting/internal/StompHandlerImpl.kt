package com.kova700.bookchat.core.stomp.chatting.internal

import android.util.Log
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.stomp.chatting.external.SocketMessageHandler
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.core.stomp.chatting.external.model.CommonMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.RequestSendChat
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.external.model.SubscriptionState
import com.kova700.bookchat.core.stomp.internal.BuildConfig
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.core.domain.usecase.chat.GetChatUseCase
import com.kova700.core.domain.usecase.client.RenewBookChatTokenUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.hildan.krossbow.stomp.ConnectionException
import org.hildan.krossbow.stomp.MissingHeartBeatException
import org.hildan.krossbow.stomp.SessionDisconnectedException
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompErrorFrameReceived
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.WebSocketClosedUnexpectedly
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.websocket.WebSocketException
import java.util.UUID
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

// TODO : [Version 2] 채널 구독 형식이 아닌 소켓 연결 후, 모든 채널 메세지 받는 형식으로 수정
//	      (모든 채널을 구독하게 되는 경우 구독 형식을 사용할 필요가 없어짐)
//	      subscribedChannelInfos 제거

//TODO : [FixWaiting] 최종적으로 채팅 무식하게 계속 쳐보고 실패한 채팅도 재전송되고,
// 소켓 끊기면 재연결 되고, 예외터져도 앱이 안죽는지 Check,
// 홈 나갔다와도 리커넥션 잘 되는지 동기화는 잘 되는지

// TODO : [FixWaiting] 다른 채팅방에서 채팅을 보고 있을때, Notification눌러서 다른 채팅방으로 가면 소켓연결이 안됨
// TODO : [FixWaiting] 위 상황대로 다른 채팅방 갔다가 다시 이전 채팅방으로 가면 채팅이 보였다가 사라지는 현상이 있음

// TODO : [FixWaiting] "존재하지 않는 채팅방" 저장되는 현상있고 로그도 찍힘
//  방장의 채팅방 나가기 시, 목록에서도 사라지지 않음

// TODO : [FixWaiting] FCM 수신안되는 이슈
//  빌드를 좀 많이 하다보면 어느순간 FCM 수신이 안되는 현상이 있음

class StompHandlerImpl @Inject constructor(
	private val stompClient: StompClient,
	private val chatRepository: ChatRepository,
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val socketMessageHandler: SocketMessageHandler,
	private val renewBookChatTokenUseCase: RenewBookChatTokenUseCase,
	private val getChatUserCase: GetChatUseCase,
	private val networkManager: NetworkManager,
	private val jsonSerializer: Json,
) : StompHandler {

	private lateinit var stompSession: StompSession
	private val _socketState = MutableStateFlow<SocketState>(SocketState.DISCONNECTED)

	private fun setSocketState(state: SocketState) {
		_socketState.update { state }
	}

	private val isAlreadyConnectedOrConnecting
		get() = _socketState.value == SocketState.CONNECTING
						|| _socketState.value == SocketState.CONNECTED

	override val isSocketConnected
		get() = _socketState.value == SocketState.CONNECTED

	private val subscriptionStates =
		MutableStateFlow<Map<Long, SubscriptionState>>(emptyMap()) // (channelId, SubscriptionState)

	private val socketConnectingMutex = Mutex()
	private val subscribingMutex = Mutex()

	private fun isChannelAlreadySubscribedOrSubscribing(channelId: Long): Boolean {
		return subscriptionStates.value[channelId]?.let { state ->
			state == SubscriptionState.SUBSCRIBING || state == SubscriptionState.SUBSCRIBED
		} ?: false
	}

	private fun setSubscriptionStates(newStates: Map<Long, SubscriptionState>) {
		subscriptionStates.update { newStates }
	}

	override fun getSocketStateFlow(): Flow<SocketState> {
		return _socketState.asStateFlow()
	}

	override fun getChannelSubscriptionStateFlow(channelId: Long): Flow<SubscriptionState> {
		return subscriptionStates.map { it[channelId] ?: SubscriptionState.UNSUBSCRIBED }
	}

	/** 실패 시 지수백오프 커넥션 요청*/
	override suspend fun connectSocket(maxAttempts: Int) {
		socketConnectingMutex.withLock {
			if (isAlreadyConnectedOrConnecting) return
			setSocketState(SocketState.CONNECTING)
		}
		Log.d(TAG, "StompHandlerImpl: connectSocket() - called - 실제 연결 작업 시작")

		var haveTriedRenewingToken = false
		for (attempt in 0 until maxAttempts) {
			Log.d(TAG, "StompHandlerImpl: connectSocket() - attempt : $attempt")
			if (networkManager.isNetworkAvailable().not()) {
				setSocketState(SocketState.FAILURE)
				return
			}

			runCatching {
				stompClient.connect(
					url = BuildConfig.STOMP_CONNECTION_URL,
					customStompConnectHeaders = stompHeader
				)
			}.onSuccess {
				stompSession = it
				setSocketState(SocketState.CONNECTED)
				return
			}.onFailure { throwable ->
				if ((throwable is ConnectionException) && haveTriedRenewingToken.not()) {
					runCatching { renewBookChatTokenUseCase() }
						.onSuccess { haveTriedRenewingToken = true }
				}
				Log.d(TAG, "StompHandlerImpl: connectSocket().onFailure() - throwable :$throwable")
			}
			delay((DEFAULT_CONNECTION_ATTEMPT_DELAY_TIME * (1.5).pow(attempt)))
		}
		setSocketState(SocketState.FAILURE)
		subscriptionStates.update { emptyMap() }
	}

	//TODO: [FixWaiting] 지수백오프 엄청 빨리 끝나는 현상이있음
	// (최대 5번 시도하는데 1초만에 끝남 코루틴 Context때문일 수도 있음)
	/** LostReceiptException으로 실패 시에만 지수백오프 구독 요청 */
	override suspend fun subscribeChannel(
		channelId: Long,
		channelSId: String,
		maxAttempts: Int,
	) {
		fun setState(state: SubscriptionState) {
			setSubscriptionStates(subscriptionStates.value + (channelId to state))
		}
		if (isSocketConnected.not()) return

		subscribingMutex.withLock {
			if (isChannelAlreadySubscribedOrSubscribing(channelId)) return
			setState(SubscriptionState.SUBSCRIBING)
		}

		for (attempt in 0 until maxAttempts) {
			Log.d(
				TAG, "StompHandlerImpl: subscribeChannel() - " +
								"channelId : $channelId, attempt : $attempt"
			)
			if (networkManager.isNetworkAvailable().not()
				|| isSocketConnected.not()
			) {
				subscriptionStates.update { emptyMap() }
				return
			}

			runCatching {
				stompSession.subscribe(
					StompSubscribeHeaders(
						destination = "$SUBSCRIBE_CHANNEL_DESTINATION${channelSId}",
						receipt = UUID.randomUUID().toString()
					)
				)
			}.onSuccess { messagesFlow ->
				//TODO :[FixWaiting] 구독이 완벽하게 되지 않았더라도 소켓이 연결되어있다면 특정 토픽으로 메세지는 보낼 수 있음 (정상 작동하는 지 확인해볼 것)
				// 이걸로도 타이밍이 완벽하지 않음 (소켓 연결이 완벽히 재연결 되었을 떄, 실패된 채팅들 전송되게)
				Log.d(TAG, "StompHandlerImpl: subscribeChannel() - channelId : $channelId, success")
				setState(SubscriptionState.SUBSCRIBED)
				retrySendFailedChats(channelId) //TODO : 자동 재전송안되는 현상있음
				messagesFlow
					.catch { handleSocketError("subscribeChannel", it) }
					.map { it.bodyAsText.parseToSocketMessage() }
					.collect { socketMessage -> socketMessageHandler.handleSocketMessage(socketMessage) }
				setSubscriptionStates(subscriptionStates.value - channelId)
				return
			}
			/** Recipt 수신 대기 시간이 있음으로 Delay Time 불필요*/
		}
		Log.d(TAG, "StompHandlerImpl: subscribeChannel() - channelId : $channelId, failed")
		setState(SubscriptionState.FAILED)
	}

	private suspend fun retrySendFailedChats(channelId: Long) {
		val retryRequiredChats = chatRepository.getFailedChats(channelId)
			.filter { it.state == ChatState.RETRY_REQUIRED }.reversed()
		for (chat in retryRequiredChats) retrySendMessage(chat.chatId)
	}

	override suspend fun retrySendMessage(chatId: Long) {
		if (_socketState.value != SocketState.CONNECTED) {
			connectSocket()
			return
		}
		val chat = getChatUserCase(chatId)
		runCatching {
			stompSession.sendText(
				destination = "$SEND_MESSAGE_DESTINATION${chat.channelId}",
				body = jsonSerializer.encodeToString(
					RequestSendChat(
						receiptId = chat.chatId,
						message = chat.message
					)
				)
			)
		}.onFailure { handleSocketError(caller = "retrySendMessage", it) }
	}

	override fun isChannelSubscribed(channelId: Long): Boolean {
		return isSocketConnected
						&& subscriptionStates.value[channelId] == SubscriptionState.SUBSCRIBED
	}

	override suspend fun disconnectSocket() {
		Log.d(TAG, "StompHandlerImpl: disconnectSocket() - called")
		setSocketState(SocketState.DISCONNECTED)
		runCatching { stompSession.disconnect() }
			.onFailure { handleSocketError("disconnectSocket", it) }
	}

	override suspend fun sendMessage(
		channelId: Long,
		message: String,
	) {
		Log.d(TAG, "StompHandlerImpl: sendMessage() - called")
		if (_socketState.value != SocketState.CONNECTED) {
			insertWaitingChat(
				channelId = channelId,
				message = message,
				chatState = ChatState.RETRY_REQUIRED
			)
			return
		}

		val receiptId = insertWaitingChat(
			channelId = channelId,
			message = message,
			chatState = ChatState.LOADING
		)

		runCatching {
			stompSession.sendText(
				destination = "$SEND_MESSAGE_DESTINATION$channelId",
				body = jsonSerializer.encodeToString(
					RequestSendChat(
						receiptId = receiptId,
						message = message
					)
				)
			)
		}.onFailure { handleSocketError(caller = "sendMessage", it) }
	}

	private suspend fun insertWaitingChat(
		channelId: Long,
		message: String,
		chatState: ChatState,
	): Long {
		val clientId = clientRepository.getClientProfile().id
		return chatRepository.insertWaitingChat(
			channelId = channelId,
			message = message,
			clientId = clientId,
			chatState = chatState
		)
	}

	private val stompHeader: Map<String, String>
		get() {
			val uuid = UUID.randomUUID()
			val bookchatToken = runBlocking { bookChatTokenRepository.getBookChatToken() }
			return mapOf(
				AUTHORIZATION to "${bookchatToken?.accessToken}",
				RECEIPT to uuid.toString()
			)
		}

	private fun String.parseToSocketMessage(): SocketMessage {
		val jsonObject = jsonSerializer.parseToJsonElement(this).jsonObject
		return when {
			jsonObject["senderId"] != null -> jsonSerializer.decodeFromString<CommonMessage>(this)
			else -> jsonSerializer.decodeFromString<NotificationMessage>(this)
		}
	}

	//TODO : [FixWaiting] 데이터가 꺼지면 어떤 예외가 터질까?
	// handleSocketError(caller :subscribeChannel) - throwable :org.hildan.krossbow.websocket.WebSocketException: Software caused connection abort
	private suspend fun handleSocketError(caller: String, throwable: Throwable) {
		Log.d(TAG, "StompHandlerImpl: handleSocketError(caller :$caller) - throwable :$throwable")
		when (throwable) {

			/** 일부 소비자가 더 많은 프레임을 기대하는 동안 STOMP 프레임 Flow가 Complete되면 예외가 발생합니다.
			 * ex : RECEIPT 프레임을 기다리는 동안 STOMP 프레임 흐름이 예기치 않게 완료
			 * (구독이 종료되었다는 소리)*/
			is SessionDisconnectedException -> Unit

			/** STOMP ERROR 프레임이 수신되면 예외가 발생합니다. 일반적으로 구독 채널을 통해 발생합니다.
			 * 브로커와의 연결이 끊겼을 때, 메세지를 보내는등의 어떤 행위를 하면 발생 (Connection to broker closed.)
			 * (해당 Frame을 수신하면 자동으로 소켓은 닫힘)*/
			is StompErrorFrameReceived -> { //TODO : 재연결하는게 맞겠지..?
				subscriptionStates.update { emptyMap() }
				_socketState.emit(SocketState.NEED_RECONNECTION)
			}

			/** 기본 웹소켓 연결이 부적절한 시간에 닫힐 때 발생하는 예외입니다.
			 * (unsubscribe를 하지 않은 상태에서 disconnect 호출이 된 순간 거의 발생
			 * (자의적으로 끊기는 경우 발생되어 재연결 불필요)) */
			is WebSocketClosedUnexpectedly -> {
				subscriptionStates.update { emptyMap() }
			}

			/** 예상되는 Socket HeartBeat이 서버 측으로부터 수신되지 않으면 예외가 발생합니다.
			 * (소켓이 타의적으로 끊기는 경우 발생) */
			is MissingHeartBeatException -> {
				subscriptionStates.update { emptyMap() }
				_socketState.emit(SocketState.NEED_RECONNECTION)
			}

			/** WebSocketException: 웹 소켓 수준에서 문제가 발생한 경우 발생하는 예외입니다. + 인터넷 연결을 끊어도 발생
			 * 자식 : WebSocketConnectionException  : websocket 연결 + STOMP 연결에 시간이 너무 오래 걸리는 경우 예외가 발생합니다. (+인터넷 꺼놓고 Connect 요청시에 발생)
			 * 자식 : WebSocketConnectionClosedException  : 핸드셰이크 중에 서버가 예기치 않게 연결을 닫았을 때 발생하는 예외입니다.
			 * */
			is WebSocketException -> {
				subscriptionStates.update { emptyMap() }
				_socketState.emit(SocketState.NEED_RECONNECTION)
			}

			/** 최초 connection시, stompSession가 연결되어있지 않을 떄, disconnect호출되면 발생 */
			is UninitializedPropertyAccessException -> Unit

			else -> {
				Log.d(TAG, "StompHandlerImpl: handleSocketError() - else throwable :$throwable")
				Unit
			}
		}
	}

	companion object {
		private const val AUTHORIZATION = "Authorization"
		private const val RECEIPT = "receipt"
		private const val SEND_MESSAGE_DESTINATION = "/subscriptions/send/chatrooms/"
		private const val SUBSCRIBE_CHANNEL_DESTINATION = "/topic/"
		private val DEFAULT_CONNECTION_ATTEMPT_DELAY_TIME = 1.seconds
	}

}