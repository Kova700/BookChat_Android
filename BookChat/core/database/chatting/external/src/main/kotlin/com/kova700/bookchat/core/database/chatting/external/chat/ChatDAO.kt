package com.kova700.bookchat.core.database.chatting.external.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus
import com.kova700.bookchat.core.data.chat.external.model.SUCCESS_CHAT_STATUS_CODE
import com.kova700.bookchat.core.database.chatting.external.chat.model.CHAT_ENTITY_TABLE_NAME
import com.kova700.bookchat.core.database.chatting.external.chat.model.ChatEntity
import com.kova700.bookchat.core.database.chatting.external.combined.ChatWithUser
import com.kova700.bookchat.util.date.getCurrentDateTimeString

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
						"WHERE channel_id = :channelId " +
						"ORDER BY status ASC, chat_id DESC " +
						"LIMIT :size"
	)
	@Transaction
	//warning: The return value includes a POJO with a @Relation. It is usually desired to annotate this method with @Transaction to avoid possibility of inconsistent results between the POJO and its relations. See https://developer.android.com/reference/androidx/room/Transaction.html for details.
	//    public abstract java.lang.Object getNewestChats(long channelId, int size, @org.jetbrains.annotations.NotNull()
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
						"WHERE channel_id = :channelId " +
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
			channelId = channelId,
			senderId = clientId,
			dispatchTime = getCurrentDateTimeString(),
			status = chatStatus.code,
			message = message,
		)
		return insertChat(chat)
	}

	@Query(
		"DELETE FROM Chat " +
						"WHERE channel_id = :channelId"
	)
	suspend fun deleteChannelAllChat(channelId: Long)

	@Query("DELETE FROM $CHAT_ENTITY_TABLE_NAME")
	suspend fun deleteAll()

	companion object {
		private const val WAITING_CHAT_MIN_ID = -1_000_000_000L
	}
}