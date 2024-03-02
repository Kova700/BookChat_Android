package com.example.bookchat.data.database.model.combined

import androidx.room.Embedded
import androidx.room.Relation
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.UserEntity

data class ChannelWithInfo(
	@Embedded
	val channelEntity: ChannelEntity,
	@Relation(parentColumn = "last_chat_id", entityColumn = "chat_id")
	val chatEntity: ChatEntity,
	@Relation(parentColumn = "host_id", entityColumn = "id")
	val hostUserEntity: UserEntity? = null,
	@Relation(parentColumn = "sub_host_ids", entityColumn = "id")
	val subHostUserEntities: List<UserEntity>? = null,
	@Relation(parentColumn = "guest_ids", entityColumn = "id")
	val guestUserEntities: List<UserEntity>? = null,
)