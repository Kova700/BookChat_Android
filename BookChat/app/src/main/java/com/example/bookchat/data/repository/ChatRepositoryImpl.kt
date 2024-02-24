package com.example.bookchat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.bookchat.BuildConfig
import com.example.bookchat.data.Chat
import com.example.bookchat.data.MessageType
import com.example.bookchat.data.SocketMessage
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.BookChatDB
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.database.dao.ChatRoomDAO
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.ChatWithUser
import com.example.bookchat.data.mapper.toChatEntity
import com.example.bookchat.data.paging.remotemediator.ChatRemoteMediator
import com.example.bookchat.data.request.RequestSendChat
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.SearchSortOption
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ChatRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val bookChatDB: BookChatDB,
	private val stompClient: StompClient,
	private val chatDAO: ChatDAO,
	private val chatRoomDAO: ChatRoomDAO,
	private val clientRepository: ClientRepository,
	private val gson: Gson
) : ChatRepository {

	private lateinit var stompSession: StompSession

	// TODO :SEND를 제외한 모든 Frame에 Receipt받게 헤더 수정 필요함

	override fun getPagedChatFlow(roomId: Long): Flow<PagingData<ChatWithUser>> {
		return Pager(
			config = PagingConfig(pageSize = LOCAL_DATA_CHAT_LOAD_SIZE),
			remoteMediator = ChatRemoteMediator(
				chatRoomId = roomId,
				chatRepository = this,
			),
			pagingSourceFactory = { chatDAO.getChatWithUserPagingSource(roomId) }
		).flow
	}

	override suspend fun connectSocket(roomSid: String, roomId: Long): Flow<SocketMessage> {
		stompSession = getStompSession()
		return subscribeChatTopic(
			stompSession = stompSession,
			roomSid = roomSid,
			roomId = roomId
		)
	}

	override suspend fun disconnectSocket() {
		stompSession.disconnect()
	}

	private suspend fun getStompSession(): StompSession {
		return stompClient.connect(
			url = BuildConfig.STOMP_CONNECTION_URL,
			customStompConnectHeaders = getHeader()
		)
	}

	//이렇게 보내면 토큰 자동 갱신은 어케 하누?
	private suspend fun subscribeChatTopic(
		stompSession: StompSession,
		roomSid: String,
		roomId: Long
	): Flow<SocketMessage> {
		return stompSession.subscribe(
			StompSubscribeHeaders(
				destination = "$SUB_CHAT_ROOM_DESTINATION$roomSid",
				customHeaders = getHeader()
			)
		).map { it.bodyAsText.parseToSocketMessage() }
			.onEach { socketMessage ->
				saveChatInLocalDB(
					socketMessage = socketMessage,
					roomId = roomId
				)
			}
	}

	private suspend fun saveChatInLocalDB(
		socketMessage: SocketMessage,
		roomId: Long
	) {
		when (socketMessage) {
			is SocketMessage.CommonMessage -> {
				saveCommonMessageInLocalDB(
					socketMessage = socketMessage,
					roomId = roomId
				)
			}

			is SocketMessage.NotificationMessage -> {
				saveNoticeMessageInLocalDB(
					socketMessage = socketMessage,
					roomId = roomId
				)
			}
		}
	}

	private suspend fun saveCommonMessageInLocalDB(
		socketMessage: SocketMessage.CommonMessage,
		roomId: Long
	) {
		val myUserId = clientRepository.getClientProfile().userId
		val receiptId = socketMessage.receiptId
		val chatEntity = socketMessage.toChatEntity(
			chatRoomId = roomId,
			myUserId = myUserId
		)

		bookChatDB.withTransaction {
			if (socketMessage.senderId != myUserId) {
				insertNewChat(chatEntity)
				updateChatRoomLastChatInfo(chatEntity)
				return@withTransaction
			}
			updateChatInfo(chatEntity, receiptId)
			updateChatRoomLastChatInfo(chatEntity)
		}
	}

	private suspend fun saveNoticeMessageInLocalDB(
		socketMessage: SocketMessage.NotificationMessage,
		roomId: Long
	) {
		val myUserId = clientRepository.getClientProfile().userId
		val chatEntity = socketMessage.toChatEntity(
			chatRoomId = roomId,
			myUserId = myUserId
		)
		// TODO :Target에 대한 DB수정 작업해야함
		bookChatDB.withTransaction {
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
				MessageType.CHAT -> {}
				MessageType.ENTER -> {
					//TODO : 채팅방 정보 조회를 다시하거나 (당첨)
					// Enter NOTICE 속에 유저 정보(채팅방 정보조회의 유저 객체처럼)가
					// 있어야할 것같음 [Id, nickname, profileUrl, defaultImageType]

					//TODO : ㅁㅁ님이 입장 하셨습니다.
					// 이것도 일반 텍스트가 아닌, UserID님이 입장하셨습니다로 보내고
					// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 입장하셨습니다로 변경하는게 좋을 듯
					chatRoomDAO.updateMemberCount(roomId, 1)
				}

				MessageType.EXIT -> {
					//TODO : 채팅방 퇴장 시에 서버에서 넣어주는 채팅 ㅁㅁ님이 퇴장 하셨습니다.
					// 이걸 일반 텍스트가 아닌, UserId님이 퇴장하셨습니다로 보내고
					// 이걸 클라이언트가 해당 유저 정보를 가지고 있는 값으로 누구님이 퇴장하셨습니다로 변경하는게 좋을 듯

					//TODO : 만약 방장이 나갔다면 채팅방 삭제 후 공지 띄우고 채팅 입력 막아야함.
					// 채팅방 인원 수 감소
					chatRoomDAO.updateMemberCount(roomId, -1)
				}

				MessageType.NOTICE_KICK -> {
					//채팅방 인원 수 감소
					chatRoomDAO.updateMemberCount(roomId, -1)
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
			insertNewChat(chatEntity)
			updateChatRoomLastChatInfo(chatEntity)
			return@withTransaction
		}
	}

	override suspend fun getLastChatOfOtherUser(roomId: Long): ChatWithUser {
		return chatDAO.getLastChatOfOtherUser(roomId)
	}

	//이렇게 보내면 토큰 자동 갱신은 어케 하누?
	override suspend fun sendMessage(
		roomId: Long,
		message: String
	) {
		val receiptId = insertWaitingChat(
			roomId = roomId,
			message = message
		)
		stompSession.send(
			StompSendHeaders(
				destination = "$SEND_MESSAGE_DESTINATION$roomId",
				customHeaders = getHeader()
			), FrameBody.Text(gson.toJson(RequestSendChat(receiptId, message)))
		)
	}

	override suspend fun getChat(
		roomId: Long,
		size: Int,
		postCursorId: Long?,
		isFirst: Boolean,
		sort: SearchSortOption
	): RespondGetChat {
		return bookChatApi.getChat(
			roomId = roomId,
			size = size,
			postCursorId = postCursorId,
			sort = sort
		).also {
			saveChatInLocalDB(
				pagedList = it.chatResponseList,
				roomId = roomId,
				isFirst = isFirst
			)
		}
	}

	private suspend fun saveChatInLocalDB(
		pagedList: List<Chat>,
		roomId: Long,
		isFirst: Boolean
	) {
		bookChatDB.withTransaction {
			chatDAO.insertAllChat(
				pagedList.toChatEntity(
					chatRoomId = roomId,
					myUserId = clientRepository.getClientProfile().userId
				)
			)
			if (isFirst) {
				val lastChat = pagedList.firstOrNull() ?: return@withTransaction
				chatRoomDAO.updateLastChatInfo(
					roomId = roomId,
					lastChatId = lastChat.chatId,
					lastActiveTime = lastChat.dispatchTime,
					lastChatContent = lastChat.message
				)
			}
		}
	}

	private suspend fun insertNewChat(chat: ChatEntity) {
		chatDAO.insertChat(chat)
	}

	private suspend fun insertWaitingChat(roomId: Long, message: String): Long {
		val myUserId = clientRepository.getClientProfile().userId
		return chatDAO.insertWaitingChat(
			roomId = roomId, message = message, myUserId = myUserId
		)
	}

	private suspend fun updateChatInfo(chat: ChatEntity, targetChatId: Long) {
		chatDAO.updateChatInfo(
			chatId = chat.chatId,
			dispatchTime = chat.dispatchTime,
			status = ChatEntity.ChatStatus.SUCCESS,
			targetChatId = targetChatId
		)
	}

	private suspend fun updateChatRoomLastChatInfo(chat: ChatEntity) {
		chatRoomDAO.updateLastChatInfo(
			roomId = chat.chatRoomId,
			lastChatId = chat.chatId,
			lastActiveTime = chat.dispatchTime,
			lastChatContent = chat.message
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
		return mapOf(
			Pair(
				AUTHORIZATION,
				"${DataStoreManager.getBookChatTokenSync().getOrNull()?.accessToken}"
			)
		)
	}

	companion object {
		private const val AUTHORIZATION = "Authorization"
		private const val SEND_MESSAGE_DESTINATION = "/subscriptions/send/chatrooms/"
		private const val SUB_CHAT_ROOM_DESTINATION = "/topic/"
		private const val LOCAL_DATA_CHAT_LOAD_SIZE = 25
	}
}