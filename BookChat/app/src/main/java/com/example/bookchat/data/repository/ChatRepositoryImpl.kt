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
	private val sortedChats = mapChats.map {
		//ORDER BY status, chat_id DESC
		it.values.toList()
			.sortedWith(
				compareBy({ chat -> chat.status }, { chat -> -chat.chatId })
			)
	}.onEach { cachedChats = it }

	private var cachedChats: List<Chat> = emptyList()
	private var cachedChannelId: Long? = null
	private var currentPage: Long? = null
	private var isEndPage = false

	override fun getChatsFlow(channelId: Long): Flow<List<Chat>> {
		return sortedChats
	}

	override suspend fun getChats(
		channelId: Long,
		size: Int
	): List<Chat> {
		if (cachedChannelId != channelId) {
			isEndPage = false
			currentPage = null
			mapChats.value = emptyMap()
		}
		if (isEndPage) return cachedChats

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

	override suspend fun getChat(chatId: Long): Chat? {
		return chatDAO.getChat(chatId)?.toChat()
	}

	override suspend fun insertChat(chat: Chat) {
		val chatId = chatDAO.insertChat(chat.toChatEntity())
		if (chat.chatRoomId != cachedChannelId) return
		val newMapChats = mapChats.value + (chatId to chat)
		mapChats.emit(newMapChats)
	}

	override suspend fun insertAllChats(chats: List<Chat>) {
		chatDAO.insertAllChat(chats.toChatEntity())
		mapChats.emit(mapChats.value + chats.associateBy { it.chatId })
	}

	override suspend fun insertWaitingChat(roomId: Long, message: String, myUserId: Long): Long {
		val chatId = chatDAO.insertWaitingChat(roomId, message, myUserId)
		val newChat = chatDAO.getChat(chatId)?.toChat() ?: return chatId
		mapChats.emit(mapChats.value + (chatId to newChat))
		return chatId
	}

	override suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long
	) {
		chatDAO.updateWaitingChat(newChatId, dispatchTime, status, targetChatId)
		val newChat = chatDAO.getChat(newChatId)?.toChat() ?: return
		mapChats.emit(mapChats.value - (targetChatId) + (newChatId to newChat))
	}

}