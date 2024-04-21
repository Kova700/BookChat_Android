package com.example.bookchat.data.repository

import com.example.bookchat.BuildConfig
import com.example.bookchat.data.MessageType
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.mapper.toChat
import com.example.bookchat.data.request.RequestSendChat
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.StompHandler
import com.example.bookchat.domain.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import javax.inject.Inject

// TODO :SEND를 제외한 모든 Frame에 Receipt받게 헤더 수정 필요함

class StompHandlerImpl @Inject constructor(
	private val stompClient: StompClient,
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val clientRepository: ClientRepository,
	private val userRepository: UserRepository,
	private val gson: Gson,
) : StompHandler {

	private lateinit var stompSession: StompSession

	override suspend fun connectSocket(channelSId: String, channelId: Long): Flow<SocketMessage> {
		stompSession = getStompSession()
		return subscribeChatTopic(
			stompSession = stompSession,
			channelSId = channelSId,
			channelId = channelId
		)
	}

	private suspend fun getStompSession(): StompSession {
		return stompClient.connect(
			url = BuildConfig.STOMP_CONNECTION_URL,
			customStompConnectHeaders = getHeader()
		)
	}

	override suspend fun disconnectSocket() {
		stompSession.disconnect()
	}

	//이렇게 보내면 토큰 자동 갱신은 어케 하누?
	override suspend fun sendMessage(
		channelId: Long,
		message: String
	) {
		val receiptId = insertWaitingChat(
			channelId = channelId,
			message = message
		)
		stompSession.send(
			StompSendHeaders(
				destination = "${SEND_MESSAGE_DESTINATION}$channelId",
				customHeaders = getHeader()
			), FrameBody.Text(gson.toJson(RequestSendChat(receiptId, message)))
		)
	}

	//이렇게 보내면 토큰 자동 갱신은 어케 하누?
	private suspend fun subscribeChatTopic(
		stompSession: StompSession,
		channelSId: String,
		channelId: Long
	): Flow<SocketMessage> {
		return stompSession.subscribe(
			StompSubscribeHeaders(
				destination = "${SUBSCRIBE_CHANNEL_DESTINATION}$channelSId",
				customHeaders = getHeader()
			)
		).map { it.bodyAsText.parseToSocketMessage() }
			.onEach { socketMessage ->
				handleSocketMessage(
					socketMessage = socketMessage,
					channelId = channelId
				)
			}
	}

	private suspend fun handleSocketMessage(
		socketMessage: SocketMessage,
		channelId: Long
	) {
		when (socketMessage) {
			is SocketMessage.CommonMessage -> {
				handleCommonMessage(
					socketMessage = socketMessage,
					channelId = channelId
				)
			}

			is SocketMessage.NotificationMessage -> {
				handleNoticeMessage(
					socketMessage = socketMessage,
					channelId = channelId
				)
			}
		}
	}

	private suspend fun handleCommonMessage(
		socketMessage: SocketMessage.CommonMessage,
		channelId: Long
	) {
		val myUserId = clientRepository.getClientProfile().id
		val receiptId = socketMessage.receiptId

		val chat = socketMessage.toChat(
			chatRoomId = channelId,
			myUserId = myUserId,
			sender = userRepository.getUser(socketMessage.senderId)
		)

		if (socketMessage.senderId != myUserId) {
			insertNewChat(chat)
			updateChannelLastChat(chat)
			return
		}

		updateWaitingChatToSuccess(chat, receiptId)
		updateChannelLastChat(chat)
	}

	private suspend fun handleNoticeMessage(
		socketMessage: SocketMessage.NotificationMessage,
		channelId: Long
	) {
		val myUserId = clientRepository.getClientProfile().id
		val chat = socketMessage.toChat(
			chatRoomId = channelId,
			myUserId = myUserId
		)
		// TODO :Target에 대한 DB수정 작업해야함
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
			MessageType.CHAT -> Unit
			MessageType.ENTER -> {
				//TODO : 채팅방 정보 조회를 다시하거나 (당첨)
				// Enter NOTICE 속에 유저 정보(채팅방 정보조회의 유저 객체처럼)가
				// 있어야할 것같음 [Id, nickname, profileUrl, defaultImageType]

				//TODO : ㅁㅁ님이 입장 하셨습니다.
				// 이것도 일반 텍스트가 아닌, UserID님이 입장하셨습니다로 보내고
				// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 입장하셨습니다로 변경하는게 좋을 듯
				channelRepository.updateMemberCount(channelId, 1)
			}

			MessageType.EXIT -> {
				//TODO : 채팅방 퇴장 시에 서버에서 넣어주는 채팅 ㅁㅁ님이 퇴장 하셨습니다.
				// 이걸 일반 텍스트가 아닌, UserId님이 퇴장하셨습니다로 보내고
				// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 퇴장하셨습니다로 변경하는게 좋을 듯

				//TODO : 만약 방장이 나갔다면 채팅방 삭제 후 공지 띄우고 채팅 입력 막아야함.
				// 채팅방 인원 수 감소
				channelRepository.updateMemberCount(channelId, -1)
			}

			MessageType.NOTICE_KICK -> {
				//채팅방 인원 수 감소
				channelRepository.updateMemberCount(channelId, -1)
			}

			MessageType.NOTICE_HOST_DELEGATE -> {
				//방장 변경
			}

			MessageType.NOTICE_SUB_HOST_DISMISS -> {
				//Target 부방장 해임
			}

			MessageType.NOTICE_SUB_HOST_DELEGATE -> {
				//Target 부방장 위임
			}
		}

		//TODO : 터질 확률이 높음
		// 유저채팅이 아니고, 프로필도 없고, defaultImageType도 없음으로로
		// if (!isFirstItemOnScreen) newOtherChatNoticeFlow.value = chatEntity
		insertNewChat(chat)
		updateChannelLastChat(chat)
	}

	private suspend fun insertNewChat(chat: Chat) {
		chatRepository.insertChat(chat)
	}

	private suspend fun insertWaitingChat(channelId: Long, message: String): Long {
		val myUserId = clientRepository.getClientProfile().id
		return chatRepository.insertWaitingChat(
			roomId = channelId, message = message, myUserId = myUserId
		)
	}

	private suspend fun updateWaitingChatToSuccess(chat: Chat, receiptId: Long) {
		chatRepository.updateWaitingChat(
			targetChatId = receiptId,
			newChatId = chat.chatId,
			dispatchTime = chat.dispatchTime,
			status = ChatStatus.SUCCESS.code
		)
	}

	private suspend fun updateChannelLastChat(chat: Chat) {
		channelRepository.updateLastChat(
			channelId = chat.chatRoomId,
			chatId = chat.chatId
		)
	}

	private fun String.parseToSocketMessage(): SocketMessage {
		runCatching { gson.fromJson(this, SocketMessage.CommonMessage::class.java) }
			.onSuccess { return it }
		runCatching { gson.fromJson(this, SocketMessage.NotificationMessage::class.java) }
			.onSuccess { return it }
		throw JsonSyntaxException("Json cannot be deserialized to SocketMessage")
	}

	private fun getHeader(): Map<String, String> {
		val bookchatToken = runBlocking { clientRepository.getBookChatToken() }
		return mapOf(AUTHORIZATION to "${bookchatToken?.accessToken}")
	}

	companion object {
		private const val AUTHORIZATION = "Authorization"
		private const val SEND_MESSAGE_DESTINATION = "/subscriptions/send/chatrooms/"
		private const val SUBSCRIBE_CHANNEL_DESTINATION = "/topic/"
		private const val LOCAL_DATA_CHAT_LOAD_SIZE = 25
	}
}