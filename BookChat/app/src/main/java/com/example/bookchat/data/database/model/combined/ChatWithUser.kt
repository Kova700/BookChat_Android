package com.example.bookchat.data.database.model.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.UserEntity

data class ChatWithUser(
	@Embedded
	val chatEntity: ChatEntity,
	@Relation(parentColumn = "sender_id", entityColumn = "id")
	val userEntity: UserEntity?,
)