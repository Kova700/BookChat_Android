package com.kova700.bookchat.core.stomp.chatting.internal

import android.util.Log
import com.kova700.bookchat.core.data.bookchat_token.external.repository.BookChatTokenRepository
import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.network_manager.external.NetworkManager
import com.kova700.bookchat.core.network_manager.external.model.NetworkState
import com.kova700.bookchat.core.stomp.chatting.external.StompHandler
import com.kova700.bookchat.core.stomp.chatting.external.model.CommonMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.NotificationMessageType
import com.kova700.bookchat.core.stomp.chatting.external.model.RequestSendChat
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketMessage
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.core.stomp.chatting.internal.mapper.toChat
import com.kova700.bookchat.core.stomp.internal.BuildConfig
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.core.domain.usecase.chat.GetChatUseCase
import com.kova700.core.domain.usecase.client.RenewBookChatTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.hildan.krossbow.stomp.ConnectionException
import org.hildan.krossbow.stomp.LostReceiptException
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

//TODO : [Version 2] Socket은 채팅방과 관계없이 연결해두고, 채팅방을 복수로 subscribe하는것도 방법 중 하나일 듯

//TODO : [FixWaiting] 최종적으로 채팅 무식하게 계속 쳐보고 실패한 채팅도 재전송되고, 소켓 끊기면 재연결 되고, 예외터져도 앱이 안죽는지 Check
//      홈 나갔다와도 리커넥션 잘 되는지 동기화는 잘 되는지
class StompHandlerImpl @Inject constructor(
	private val stompClient: StompClient,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val clientRepository: ClientRepository,
	private val bookChatTokenRepository: BookChatTokenRepository,
	private val userRepository: UserRepository,
	private val renewBookChatTokenUseCase: RenewBookChatTokenUseCase,
	private val getChatUserCase: GetChatUseCase,
	private val networkManager: NetworkManager,
	private val jsonSerializer: Json,
) : StompHandler {

	private lateinit var stompSession: StompSession
	private val _socketState = MutableStateFlow<SocketState>(SocketState.DISCONNECTED)

	private val mutex = Mutex()
	private val channelSubscriptionContext = Job() + Dispatchers.IO

	private var connectedChannelId: Long = -1

	override fun getSocketStateFlow(): StateFlow<SocketState> {
		return _socketState.asStateFlow()
	}

	/** 실패 시 지수백오프 커넥션 요청*/
	override suspend fun connectSocket(
		channel: Channel,
		maxAttempts: Int,
	) {
		if (channel.roomSid.isBlank()) return

		mutex.withLock {
			if (_socketState.value == SocketState.CONNECTING
				|| _socketState.value == SocketState.CONNECTED
			) return
			_socketState.emit(SocketState.CONNECTING)
		}

		var haveTriedRenewingToken = false
		for (attempt in 0 until maxAttempts) {
			Log.d(TAG, "StompHandlerImpl: connectSocket() - attempt : $attempt")
			if (networkManager.getStateFlow().first() == NetworkState.DISCONNECTED) return

			runCatching {
				stompClient.connect(
					url = BuildConfig.STOMP_CONNECTION_URL,
					customStompConnectHeaders = getHeader()
				)
			}.onSuccess {
				stompSession = it
				subscribeChannel(channel, maxAttempts)
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
		_socketState.emit(SocketState.FAILURE)
	}

	/** LostReceiptException으로 실패 시에만 지수백오프 구독 요청 */
	/** + 현재 라이브러리 사정상 소켓 연결 여부를 확인할 수 없음으로 구독이 끊기면 disconnect 요청 */
	private suspend fun subscribeChannel(
		channel: Channel,
		maxAttempts: Int,
	) {
		for (attempt in 0 until maxAttempts) {
			Log.d(TAG, "StompHandlerImpl: subscribeChannel() - attempt : $attempt")
			if (networkManager.getStateFlow().first() == NetworkState.DISCONNECTED) return

			runCatching {
				stompSession.subscribe(
					StompSubscribeHeaders(
						destination = "$SUBSCRIBE_CHANNEL_DESTINATION${channel.roomSid}",
						receipt = UUID.randomUUID().toString()
					)
				)
			}.onSuccess { messagesFlow ->
				_socketState.emit(SocketState.CONNECTED)
				connectedChannelId = channel.roomId

				val channelSubscription =
					CoroutineScope(channelSubscriptionContext).launch {
						messagesFlow
							.catch { handleSocketError("subscribeChannel", it) }
							.map { it.bodyAsText.parseToSocketMessage() }
							.collect { socketMessage ->
								handleSocketMessage(
									socketMessage = socketMessage,
									channelId = channel.roomId
								)
							}
					}
				retrySendFailedChats(channel.roomId) //TODO : [FixWaiting] 이걸로도 타이밍이 완벽하지 않음 (소켓 연결이 완벽히 재연결 되었을 떄, 실패된 채팅들 전송되게)
				channelSubscription.join()

				disconnectSocket()
				return

			}.onFailure { throwable ->
				if (throwable !is LostReceiptException) {
					_socketState.emit(SocketState.FAILURE)
					return
				}
			}
			/** Recipt 수신 대기 시간이 있음으로 Delay Time 불필요*/
		}
		_socketState.emit(SocketState.FAILURE)
	}

	private suspend fun retrySendFailedChats(channelId: Long) {
		val retryRequiredChats = chatRepository.getFailedChats(channelId)
			.filter { it.status == ChatStatus.RETRY_REQUIRED }.reversed()
		for (chat in retryRequiredChats) {
			retrySendMessage(chat.chatId)
		}
	}

	override suspend fun retrySendMessage(chatId: Long) {
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

	override fun isSocketConnected(channelId: Long): Boolean {
		return _socketState.value == SocketState.CONNECTED
						&& connectedChannelId == channelId
	}

	override suspend fun disconnectSocket() {
		_socketState.emit(SocketState.DISCONNECTED)
		connectedChannelId = -1
		Log.d(TAG, "StompHandlerImpl: disconnectSocket() - called")
		runCatching { stompSession.disconnect() }
			.onFailure { handleSocketError("disconnectSocket", it) }
	}

	override suspend fun sendMessage(
		channelId: Long,
		message: String,
	) {
		Log.d(
			TAG,
			"StompHandlerImpl: sendMessage() - message: $message , _socketState : ${_socketState.value}"
		)
		if (_socketState.value != SocketState.CONNECTED) {
			Log.d(TAG, "StompHandlerImpl: sendMessage() - SocketState.DISCONNECTED : 실패로 삽입")
			insertWaitingChat(
				channelId = channelId,
				message = message,
				chatStatus = ChatStatus.RETRY_REQUIRED
			)
			return
		}

		val receiptId = insertWaitingChat(
			channelId = channelId,
			message = message,
			chatStatus = ChatStatus.LOADING
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

	private suspend fun handleSocketMessage(
		socketMessage: SocketMessage,
		channelId: Long,
	) {
		when (socketMessage) {
			is CommonMessage -> handleCommonMessage(
				socketMessage = socketMessage,
				channelId = channelId
			)

			is NotificationMessage -> handleNoticeMessage(
				socketMessage = socketMessage,
				channelId = channelId
			)
		}
	}

	private suspend fun handleCommonMessage(
		socketMessage: CommonMessage,
		channelId: Long,
	) {
		val clientId = clientRepository.getClientProfile().id
		val receiptId = socketMessage.receiptId

		val chat = socketMessage.toChat(
			channelId = channelId,
			sender = userRepository.getUser(socketMessage.senderId)
		)

		if (socketMessage.senderId == clientId) {
			updateWaitingChat(chat, receiptId)
			updateChannelLastChat(chat)
			return
		}

		insertNewChat(chat)
		updateChannelLastChat(chat)
	}

	private suspend fun handleNoticeMessage(
		socketMessage: NotificationMessage,
		channelId: Long,
	) {
		val chat = socketMessage.toChat(
			channelId = channelId,
		)

		when (socketMessage.notificationMessageType) {
			NotificationMessageType.NOTICE_ENTER -> {
				channelRepository.enterChannelMember(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return
				)
			}

			NotificationMessageType.NOTICE_EXIT -> {
				channelRepository.leaveChannelMember(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return
				)
			}

			NotificationMessageType.NOTICE_HOST_EXIT ->
				channelRepository.leaveChannelHost(channelId)

			NotificationMessageType.NOTICE_KICK -> {
				val clientId = clientRepository.getClientProfile().id
				val isClientBanned = socketMessage.targetUserId == clientId
				when {
					isClientBanned -> channelRepository.banChannelClient(channelId)
					else -> channelRepository.leaveChannelMember(
						channelId = channelId,
						targetUserId = socketMessage.targetUserId ?: return
					)
				}
			}

			NotificationMessageType.NOTICE_HOST_DELEGATE -> {
				channelRepository.updateChannelHost(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
				)
			}

			NotificationMessageType.NOTICE_SUB_HOST_DELEGATE -> {
				channelRepository.updateChannelMemberAuthority(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
					channelMemberAuthority = ChannelMemberAuthority.SUB_HOST,
				)
			}

			NotificationMessageType.NOTICE_SUB_HOST_DISMISS ->
				channelRepository.updateChannelMemberAuthority(
					channelId = channelId,
					targetUserId = socketMessage.targetUserId ?: return,
					channelMemberAuthority = ChannelMemberAuthority.GUEST,
				)

		}
		insertNewChat(chat)
		updateChannelLastChat(chat)
	}

	private suspend fun insertNewChat(chat: Chat) {
		chatRepository.insertChat(chat)
	}

	private suspend fun insertWaitingChat(
		channelId: Long,
		message: String,
		chatStatus: ChatStatus,
	): Long {
		val clientId = clientRepository.getClientProfile().id
		return chatRepository.insertWaitingChat(
			channelId = channelId,
			message = message,
			clientId = clientId,
			chatStatus = chatStatus
		)
	}

	private suspend fun updateWaitingChat(chat: Chat, receiptId: Long) {
		Log.d(TAG, "StompHandlerImpl: updateWaitingChatToSuccess() - receiptId :$receiptId")
		chatRepository.updateWaitingChat(
			newChat = chat,
			receiptId = receiptId
		)
	}

	private suspend fun updateChannelLastChat(chat: Chat) {
		channelRepository.updateChannelLastChatIfValid(
			channelId = chat.channelId,
			chatId = chat.chatId
		)
	}

	private fun getHeader(): Map<String, String> {
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

	private suspend fun handleSocketError(caller: String, throwable: Throwable) {
		Log.d(TAG, "StompHandlerImpl: handleSocketError(caller :$caller) - throwable :$throwable")
		when (throwable) {

			/** 일부 소비자가 더 많은 프레임을 기대하는 동안 STOMP 프레임 Flow가 Complete되면 예외가 발생합니다.
			 * ex : RECEIPT 프레임을 기다리는 동안 STOMP 프레임 흐름이 예기치 않게 완료 */
			is SessionDisconnectedException -> Unit

			/** STOMP ERROR 프레임이 수신되면 예외가 발생합니다. 일반적으로 구독 채널을 통해 발생합니다.
			 * 브로커와의 연결이 끊겼을 때, 메세지를 보내는등의 어떤 행위를 하면 발생 (Connection to broker closed.)
			 * (소켓은 연결되었지만 구독되지않았을 때 터짐 == 자동으로 닫힘)*/
			is StompErrorFrameReceived -> Unit

			/** 기본 웹소켓 연결이 부적절한 시간에 닫힐 때 발생하는 예외입니다.
			 * (unsubscribe를 하지 않은 상태에서 disconnect 호출이 된 순간 거의 발생
			 * (자의적으로 끊기는 경우 발생되어 재연결 불필요)) */
			is WebSocketClosedUnexpectedly -> Unit

			/** 예상되는 Socket HeartBeat이 서버 측으로부터 수신되지 않으면 예외가 발생합니다.
			 * (소켓이 타의적으로 끊기는 경우 발생) */
			is MissingHeartBeatException -> _socketState.emit(SocketState.NEED_RECONNECTION)

			/** WebSocketException: 웹 소켓 수준에서 문제가 발생한 경우 발생하는 예외입니다. + 인터넷 연결을 끊어도 발생
			 * 자식 : WebSocketConnectionException  : websocket 연결 + STOMP 연결에 시간이 너무 오래 걸리는 경우 예외가 발생합니다. (+인터넷 꺼놓고 Connect 요청시에 발생)
			 * 자식 : WebSocketConnectionClosedException  : 핸드셰이크 중에 서버가 예기치 않게 연결을 닫았을 때 발생하는 예외입니다.
			 * */
			is WebSocketException -> _socketState.emit(SocketState.NEED_RECONNECTION)

			/** 최초 connection시, stompSession가 연결되어있지 않을 떄, disconnect호출되면 발생 */
			is UninitializedPropertyAccessException -> Unit

			else -> Unit
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