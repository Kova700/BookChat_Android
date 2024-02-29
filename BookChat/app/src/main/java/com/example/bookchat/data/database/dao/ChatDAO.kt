package com.example.bookchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.utils.DateManager
import kotlin.math.max

@Dao
interface ChatDAO {

	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :channelId " +
						"ORDER BY status, chat_id DESC"
	)
	suspend fun getLastChatInChannel(channelId: Long): ChatWithUser

	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :channelId " +
						"ORDER BY status, chat_id DESC"
	)
	suspend fun getChatWithUsersInChannel(channelId: Long): List<ChatWithUser>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAllChat(chats: List<ChatEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChat(chat: ChatEntity): Long

	@Query(
		"UPDATE Chat SET " +
						"chat_id = :newChatId, " +
						"dispatch_time = :dispatchTime, " +
						"status = :status " +
						"WHERE chat_id = :targetChatId"
	)
	suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long,
	)

	@Query(
		"SELECT MAX(chat_id) FROM Chat " +
						"WHERE chat_id BETWEEN $MIN_CHAT_ID AND 0"
	)
	suspend fun getMaxWaitingChatId(): Long?

	suspend fun insertWaitingChat(
		roomId: Long,
		message: String,
		myUserId: Long
	): Long {
		val chat = ChatEntity(
			chatId = getWaitingChatId(),
			chatRoomId = roomId,
			senderId = myUserId,
			dispatchTime = DateManager.getCurrentDateTimeString(),
			status = ChatStatus.LOADING.code,
			message = message,
			chatType = ChatType.Mine,
		)
		return insertChat(chat)
	}

	private suspend fun getWaitingChatId(): Long {
		maxWaitingChatId =
			max(maxWaitingChatId, getMaxWaitingChatId()?.plus(1) ?: maxWaitingChatId)
		return maxWaitingChatId++
	}

	companion object {
		private const val MIN_CHAT_ID = -1_000_000_000L
		private var maxWaitingChatId = MIN_CHAT_ID
	}
}