package com.example.bookchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookchat.data.database.model.CHAT_ENTITY_TABLE_NAME
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.domain.model.SUCCESS_CHAT_STATUS_CODE
import com.example.bookchat.utils.DateManager

@Dao
interface ChatDAO {

	@Transaction
	@Query("SELECT * FROM Chat WHERE chat_id = :chatId")
	suspend fun getChat(chatId: Long): ChatWithUser?

	@Transaction
	@Query(
		"SELECT * FROM Chat WHERE chat_id IN (:chatIds) " +
						"ORDER BY status ASC, chat_id DESC "
	)
	suspend fun getChats(chatIds: List<Long>): List<ChatWithUser>

	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :channelId " +
						"ORDER BY status ASC, chat_id DESC " +
						"LIMIT :size"
	)
	suspend fun getNewestChats(channelId: Long, size: Int): List<ChatWithUser>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChats(chats: List<ChatEntity>): List<Long>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChat(chat: ChatEntity): Long

	@Query("DELETE FROM Chat WHERE chat_id = :targetChatId")
	suspend fun deleteChat(targetChatId: Long)

	@Query(
		"SELECT MAX(chat_id) FROM Chat " +
						"WHERE chat_id BETWEEN $WAITING_CHAT_MIN_ID AND 0"
	)
	/**존재하는 Fail 혹은 Waiting Chat들 중 가장 높은 Id getter (없다면 null 반환)*/
	suspend fun getMaxWaitingChatId(): Long?

	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :channelId " +
						"AND status < $SUCCESS_CHAT_STATUS_CODE " +
						"ORDER BY status ASC, chat_id DESC"
	)
	suspend fun getChannelsFailedChats(channelId: Long): List<ChatEntity>

	suspend fun insertWaitingChat(
		channelId: Long,
		message: String,
		clientId: Long,
		chatStatus: ChatStatus,
	): Long {
		val chat = ChatEntity(
			chatId = getMaxWaitingChatId()?.plus(1) ?: WAITING_CHAT_MIN_ID,
			chatRoomId = channelId,
			senderId = clientId,
			dispatchTime = DateManager.getCurrentDateTimeString(),
			status = chatStatus.code,
			message = message,
			chatType = ChatType.Mine,
		)
		return insertChat(chat)
	}

	@Query(
		"DELETE FROM Chat " +
						"WHERE chat_room_id = :channelId"
	)
	suspend fun deleteChannelAllChat(channelId: Long)

	@Query("DELETE FROM $CHAT_ENTITY_TABLE_NAME")
	suspend fun deleteAll()

	companion object {
		private const val WAITING_CHAT_MIN_ID = -1_000_000_000L
	}
}