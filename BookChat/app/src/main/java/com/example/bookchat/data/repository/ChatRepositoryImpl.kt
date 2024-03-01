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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val chatDAO: ChatDAO,
	private val clientRepository: ClientRepository,
) : ChatRepository {

	private val mapChats = MutableStateFlow<Map<Long, Chat>>(emptyMap()) //(chatId, Chat)
	private val sortedChats = mapChats
		.map {
			it.values.toList()
				.sortedWith(
					compareBy({ chat -> chat.status }, { chat -> -chat.chatId })
				)
		}
		.onEach { cachedChat = it }
	private var cachedChat: List<Chat> = emptyList()

	private var cachedChannelId: Long? = null
	private var currentPage: Long? = null
	private var isEndPage = false

	override suspend fun getChatFlow(): Flow<List<Chat>> {
		return sortedChats
	}

	override suspend fun getChats(
		channelId: Long,
		size: Int
	): List<Chat> {
		if (cachedChannelId != channelId) {
			//TODO : 추후 채팅방별 isEndPage , currentPage, mapChats 캐싱 해놓고 다시 가져다 쓸 수 있게 수정
			isEndPage = false
			currentPage = null
			mapChats.value = emptyMap()
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

		val newChats = response.chatResponseList.map {
			it.toChat(
				chatRoomId = channelId,
				myUserId = clientRepository.getClientProfile().id
			)
		}
		insertAllChats(newChats)
		return newChats
	}

	override suspend fun insertChat(chat: Chat) {
		val chatId = chatDAO.insertChat(chat.toChatEntity())
		val newChat = chatDAO.getChat(chatId).toChat()
		val newMapChats = mapChats.value + (chatId to newChat)
		mapChats.emit(newMapChats)
	}

	override suspend fun insertAllChats(chats: List<Chat>) {
		val chatIds = chatDAO.insertAllChat(chats.toChatEntity())
		val newMapChats = mapChats.value + chatDAO.getChats(chatIds)
			.associate { it.chatEntity.chatId to it.toChat() }
		mapChats.emit(newMapChats)
	}

	override suspend fun insertWaitingChat(roomId: Long, message: String, myUserId: Long): Long {
		val chatId = chatDAO.insertWaitingChat(roomId, message, myUserId)
		val newChat = chatDAO.getChat(chatId).toChat()
		val newMapChats = mapChats.value + (chatId to newChat)
		mapChats.emit(newMapChats)
		return chatId
	}

	override suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long
	) {
		chatDAO.updateWaitingChat(newChatId, dispatchTime, status, targetChatId)
		val newChat = chatDAO.getChat(newChatId).toChat()
		val newMapChats = mapChats.value - (targetChatId) + (newChatId to newChat)
		mapChats.emit(newMapChats)
	}

}