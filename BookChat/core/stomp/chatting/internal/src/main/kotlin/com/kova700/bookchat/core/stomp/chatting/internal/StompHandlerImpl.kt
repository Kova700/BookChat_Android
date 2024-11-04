package com.kova700.bookchat.core.stomp.chatting.internal

import android.util.Log
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.stomp.chatting.external.SocketMessageHandler
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.core.stomp.chatting.external.model.ChannelSubscriptionFailureException
import com.kova700.bookchat.core.stomp.chatting.external.model.CommonMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.DuplicateSocketConnectionException
import com.kova700.bookchat.core.stomp.chatting.external.model.DuplicateSubscriptionRequestException
import com.kova700.bookchat.core.stomp.chatting.external.model.NetworkUnavailableException
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.RequestSendChat
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketConnectionFailureException
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
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
import org.hildan.krossbow.stomp.frame.StompFrame
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

class StompHandlerImpl @Inject constructor(
	private val stompClient: StompClient,
	private val chatRepository: ChatRepository,
	private val channelRepository: ChannelRepository,
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val socketMessageHandler: SocketMessageHandler,
	private val renewBookChatTokenUseCase: RenewBookChatTokenUseCase,
	private val getChatUseCase: GetChatUseCase,
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
		return subscriptionStates
			.map { it[channelId] ?: SubscriptionState.UNSUBSCRIBED }
			.distinctUntilChanged()
	}

	/** 실패 시 지수백오프 커넥션 요청*/
	override suspend fun connectSocket(maxAttempts: Int) {
		socketConnectingMutex.withLock {
			if (isAlreadyConnectedOrConnecting) throw DuplicateSocketConnectionException()
			setSocketState(SocketState.CONNECTING)
		}
		Log.d(TAG, "StompHandlerImpl: connectSocket() - called - 실제 연결 작업 시작")

		var haveTriedRenewingToken = false
		for (attempt in 0 until maxAttempts) {
			Log.d(TAG, "StompHandlerImpl: connectSocket() - attempt : $attempt")
			if (networkManager.isNetworkAvailable().not()) {
				setSocketState(SocketState.FAILURE)
				throw NetworkUnavailableException()
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
		throw SocketConnectionFailureException()
	}

	/** LostReceiptException으로 실패 시에만 지수백오프 구독 요청 */
	override suspend fun subscribeChannel(
		channelId: Long,
		maxAttempts: Int,
	): Flow<SocketMessage> {
		fun setState(state: SubscriptionState) {
			setSubscriptionStates(subscriptionStates.value + (channelId to state))
		}

		val channel = channelRepository.getChannel(channelId)
			?: throw ChannelSubscriptionFailureException()
		val channelSId = channel.roomSid.takeIf { it.isNotBlank() }
			?: throw ChannelSubscriptionFailureException()

		if (isSocketConnected.not()) throw ChannelSubscriptionFailureException()

		subscribingMutex.withLock {
			if (isChannelAlreadySubscribedOrSubscribing(channelId)) throw DuplicateSubscriptionRequestException()
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
				throw NetworkUnavailableException()
			}

			runCatching { subscribe(channelSId) }
				.onSuccess { messagesFlow ->
					Log.d(TAG, "StompHandlerImpl: subscribeChannel() - channelId : $channelId, success")
					setState(SubscriptionState.SUBSCRIBED)
					return messagesFlow
						.catch { handleSocketError("subscribeChannel", it) }
						.map { it.bodyAsText.parseToSocketMessage() }
						.onEach { socketMessage -> socketMessageHandler.handleSocketMessage(socketMessage) }
						.onCompletion { setSubscriptionStates(subscriptionStates.value - channelId) }
				}
			delay((DEFAULT_CONNECTION_ATTEMPT_DELAY_TIME * (1.5).pow(attempt)))
		}

		Log.d(TAG, "StompHandlerImpl: subscribeChannel() - channelId : $channelId, failed")
		setState(SubscriptionState.FAILED)
		throw ChannelSubscriptionFailureException()
	}

	override suspend fun retrySendMessage(chatId: Long) {
		val chat = getChatUseCase(chatId) ?: return
		runCatching {
			send(
				channelId = chat.channelId,
				receiptId = chat.chatId,
				message = chat.message
			)
		}.onFailure {
			if (chat.state != ChatState.FAILURE) chatRepository.updateChatState(chatId, ChatState.FAILURE)
			handleSocketError(caller = "retrySendMessage", it)
		}
	}

	override fun isChannelSubscribed(channelId: Long): Boolean {
		return isSocketConnected
						&& subscriptionStates.value[channelId] == SubscriptionState.SUBSCRIBED
	}

	override suspend fun disconnectSocket() {
		if (::stompSession.isInitialized.not()) return
		Log.d(TAG, "StompHandlerImpl: disconnectSocket() - called")
		setSocketState(SocketState.DISCONNECTED)
		runCatching { stompSession.disconnect() }
			.onFailure { handleSocketError("disconnectSocket", it) }
	}

	override suspend fun sendMessage(
		channelId: Long,
		message: String,
	) {
		if (isSocketConnected.not()) {
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
			send(
				channelId = channelId,
				receiptId = receiptId,
				message = message
			)
		}.onFailure { handleSocketError(caller = "sendMessage", it) }
	}

	private suspend fun send(
		channelId: Long,
		receiptId: Long,
		message: String,
	) {
		stompSession.sendText(
			destination = "$SEND_MESSAGE_DESTINATION$channelId",
			body = jsonSerializer.encodeToString(
				RequestSendChat(
					receiptId = receiptId,
					message = message
				)
			)
		)
	}

	private suspend fun subscribe(channelSId: String): Flow<StompFrame.Message> {
		return stompSession.subscribe(
			StompSubscribeHeaders(
				destination = "$SUBSCRIBE_CHANNEL_DESTINATION${channelSId}",
				receipt = UUID.randomUUID().toString()
			)
		)
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

	private fun handleSocketError(caller: String, throwable: Throwable) {
		Log.d(TAG, "StompHandlerImpl: handleSocketError(caller :$caller) - throwable :$throwable")
		when (throwable) {

			/** 일부 소비자가 더 많은 프레임을 기대하는 동안 STOMP 프레임 Flow가 Complete되면 예외가 발생합니다.
			 * ex : RECEIPT 프레임을 기다리는 동안 STOMP 프레임 흐름이 예기치 않게 완료
			 * (구독이 종료되었다는 소리)*/
			is SessionDisconnectedException -> Unit

			/** STOMP ERROR 프레임이 수신되면 예외가 발생합니다. 일반적으로 구독 채널을 통해 발생합니다.
			 * 브로커와의 연결이 끊겼을 때, 메세지를 보내는등의 어떤 행위를 하면 발생 (Connection to broker closed.)
			 * (해당 Frame을 수신하면 자동으로 소켓은 닫힘)*/
			is StompErrorFrameReceived -> {
				subscriptionStates.update { emptyMap() }
				_socketState.update { SocketState.NEED_RECONNECTION }
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
				_socketState.update { SocketState.NEED_RECONNECTION }
			}

			/** WebSocketException: 웹 소켓 수준에서 문제가 발생한 경우 발생하는 예외입니다. + 인터넷 연결을 끊어도 발생
			 * 자식 : WebSocketConnectionException  : websocket 연결 + STOMP 연결에 시간이 너무 오래 걸리는 경우 예외가 발생합니다. (+인터넷 꺼놓고 Connect 요청시에 발생)
			 * 자식 : WebSocketConnectionClosedException  : 핸드셰이크 중에 서버가 예기치 않게 연결을 닫았을 때 발생하는 예외입니다.
			 * */
			is WebSocketException -> {
				subscriptionStates.update { emptyMap() }
				_socketState.update { SocketState.NEED_RECONNECTION }
			}

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