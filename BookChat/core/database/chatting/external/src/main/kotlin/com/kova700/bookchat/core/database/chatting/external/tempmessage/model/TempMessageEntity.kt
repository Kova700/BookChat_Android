package com.kova700.bookchat.core.database.chatting.external.tempmessage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kova700.bookchat.core.database.chatting.external.channel.model.ChannelEntity

@Entity(
	tableName = TEMP_MESSAGE_TABLE_NAME, foreignKeys = [ForeignKey(
		entity = ChannelEntity::class,
		parentColumns = arrayOf("room_id"),
		childColumns = arrayOf("chat_room_id"),
		onDelete = ForeignKey.CASCADE,
	)]
)
data class TempMessageEntity(
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "chat_room_id") val chatRoomId: Long,
	@ColumnInfo(name = "message") val message: String,
)

const val TEMP_MESSAGE_TABLE_NAME = "TempMessage"
