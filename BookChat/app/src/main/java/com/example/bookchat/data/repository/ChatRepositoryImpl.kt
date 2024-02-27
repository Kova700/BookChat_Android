package com.example.bookchat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.room.withTransaction
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.database.BookChatDB
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.database.dao.ChatDAO
import com.example.bookchat.data.mapper.toChat
import com.example.bookchat.data.mapper.toChatEntity
import com.example.bookchat.data.response.ChatResponse
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.utils.SearchSortOption
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ChatRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val chatDAO: ChatDAO,
	private val channelDAO: ChannelDAO,
	private val clientRepository: ClientRepository,
	private val bookChatDB: BookChatDB,
) : ChatRepository {

//	override fun getPagedChatFlow(roomId: Long): Flow<PagingData<Chat>> {
//		return Pager(
//			config = PagingConfig(pageSize = LOCAL_DATA_CHAT_LOAD_SIZE),
//			remoteMediator = ChatRemoteMediator(
//				chatRoomId = roomId,
//				chatRepository = this,
//			),
//			pagingSourceFactory = { chatDAO.getChatWithUserPagingSource(roomId) }
//		).flow.map { it }
//	}

	override suspend fun getLastChatOfOtherUser(roomId: Long): Chat {
		return chatDAO.getLastChatOfOtherUser(roomId).toChat()
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

	override suspend fun insertChat(chat: Chat) {
		chatDAO.insertChat(chat.toChatEntity())
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

	private suspend fun saveChatInLocalDB(
		pagedList: List<ChatResponse>,
		roomId: Long,
		isFirst: Boolean
	) {
		bookChatDB.withTransaction {
			chatDAO.insertAllChat(
				pagedList.toChatEntity(
					chatRoomId = roomId,
					myUserId = clientRepository.getClientProfile().id
				)
			)
			if (isFirst) {
				val lastChat = pagedList.firstOrNull() ?: return@withTransaction
				updateChannelLastChat(
					lastChat.toChat(
						chatRoomId = roomId,
						myUserId = clientRepository.getClientProfile().id
					)
				)
			}
		}
	}

	private suspend fun updateChannelLastChat(chat: Chat) {
		channelDAO.updateLastChat(
			roomId = chat.chatRoomId,
			lastChatId = chat.chatId
		)
	}

}