package com.kova700.bookchat.core.database.chatting.external.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.kova700.bookchat.core.database.chatting.external.chat.model.ChatEntity
import com.kova700.bookchat.core.database.chatting.external.user.model.UserEntity

data class ChatWithUser(
	@Embedded
	val chatEntity: ChatEntity,
	@Relation(parentColumn = "sender_id", entityColumn = "id")
	val userEntity: UserEntity?,
)