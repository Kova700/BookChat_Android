package com.example.bookchat.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookchat.domain.model.ChatStatus

@Entity(
	tableName = CHAT_ENTITY_TABLE_NAME,
	indices = [
		Index(value = ["channel_id"]),
		Index(value = ["status"]),
	],
)
data class ChatEntity(
	@PrimaryKey
	@ColumnInfo(name = "chat_id") val chatId: Long,
	@ColumnInfo(name = "channel_id") val channelId: Long,
	@ColumnInfo(name = "sender_id") val senderId: Long?,
	@ColumnInfo(name = "dispatch_time") val dispatchTime: String,
	@ColumnInfo(name = "message") val message: String,
	@ColumnInfo(name = "status") val status: Int = ChatStatus.SUCCESS.code,
) {
	val isRetryRequired
		get() = status == ChatStatus.RETRY_REQUIRED.code
}

const val CHAT_ENTITY_TABLE_NAME = "Chat"