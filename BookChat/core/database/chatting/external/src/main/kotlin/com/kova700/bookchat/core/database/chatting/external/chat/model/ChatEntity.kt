package com.kova700.bookchat.core.database.chatting.external.chat.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kova700.bookchat.core.data.chat.external.model.ChatState

@Entity(
	tableName = CHAT_ENTITY_TABLE_NAME,
	indices = [
		Index(value = ["channel_id"]),
		Index(value = ["state"]),
	],
)
data class ChatEntity(
	@PrimaryKey
	@ColumnInfo(name = "chat_id") val chatId: Long,
	@ColumnInfo(name = "channel_id") val channelId: Long,
	@ColumnInfo(name = "sender_id") val senderId: Long?,
	@ColumnInfo(name = "dispatch_time") val dispatchTime: String,
	@ColumnInfo(name = "message") val message: String,
	@ColumnInfo(name = "state") val state: Int,
) {
	val isRetryRequired
		get() = state == ChatState.RETRY_REQUIRED.code
}

const val CHAT_ENTITY_TABLE_NAME = "Chat"