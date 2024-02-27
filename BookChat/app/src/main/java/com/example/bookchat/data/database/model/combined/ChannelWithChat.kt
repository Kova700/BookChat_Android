package com.example.bookchat.data.database.model.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.ChatEntity

data class ChannelWithChat(
	@Embedded
	val channelEntity: ChannelEntity,
	@Relation(parentColumn = "last_chat_id", entityColumn = "chat_id")
	val chatEntity: ChatEntity? = null
)