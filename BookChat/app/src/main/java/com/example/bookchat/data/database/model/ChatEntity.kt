package com.example.bookchat.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType

@Entity(
	tableName = "Chat", foreignKeys = [ForeignKey(
		entity = ChannelEntity::class,
		parentColumns = arrayOf("room_id"),
		childColumns = arrayOf("chat_room_id"),
		onDelete = ForeignKey.CASCADE,
	)]
)
data class ChatEntity(
	@PrimaryKey
	@ColumnInfo(name = "chat_id") val chatId: Long,
	@ColumnInfo(name = "chat_room_id") val chatRoomId: Long,
	@ColumnInfo(name = "sender_id") val senderId: Long?,
	@ColumnInfo(name = "dispatch_time") val dispatchTime: String,
	@ColumnInfo(name = "message") val message: String,
	@ColumnInfo(name = "chat_type") val chatType: ChatType,
	@ColumnInfo(name = "status") val status: Int = ChatStatus.SUCCESS.code,
)