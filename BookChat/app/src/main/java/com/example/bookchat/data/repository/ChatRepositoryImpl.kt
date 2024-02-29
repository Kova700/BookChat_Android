package com.example.bookchat.data.repository

import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.mapper.toChat
import com.example.bookchat.data.mapper.toChatEntity
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val chatDAO: ChatDAO,
	private val clientRepository: ClientRepository,
) : ChatRepository {

	private val chats = MutableStateFlow<List<Chat>>(emptyList())
	private var cachedChat: List<Chat> = emptyList()
	private var cachedChannelId: Long? = null
	private var currentPage: Long? = null
	private var isEndPage = false

	override suspend fun getChatFlow(): Flow<List<Chat>> {
		return chats
	}

	override suspend fun getChats(
		channelId: Long,
		size: Int
	): List<Chat> {
		if (cachedChannelId != channelId) {
			isEndPage = false
			currentPage = null
		}
		if (isEndPage) return cachedChat

		val response = bookChatApi.getChat(
			roomId = channelId,
			postCursorId = currentPage,
			size = size
		)

		cachedChannelId = channelId
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		//채팅 DB에 저장
		insertAllChats(response.chatResponseList.map {
			it.toChat(
				chatRoomId = channelId,
				myUserId = clientRepository.getClientProfile().id
			)
		})

		//User와 Chat이 묶인 데이터로 Flow에 방출
		//TODO :페이징 사이즈 수정
		val newChatList =
			chatDAO.getChatWithUsersInChannel(channelId).map { it.toChat() }.also { cachedChat = it }
		chats.emit(newChatList)
		return cachedChat
	}

	override suspend fun insertChat(chat: Chat) {
		chatDAO.insertChat(chat.toChatEntity())
	}

	override suspend fun insertAllChats(chats: List<Chat>) {
		chatDAO.insertAllChat(chats.toChatEntity())
	}

	override suspend fun insertWaitingChat(roomId: Long, message: String, myUserId: Long): Long {
		return chatDAO.insertWaitingChat(roomId, message, myUserId)
	}

	override suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long
	) {
		chatDAO.updateWaitingChat(newChatId, dispatchTime, status, targetChatId)
	}

}