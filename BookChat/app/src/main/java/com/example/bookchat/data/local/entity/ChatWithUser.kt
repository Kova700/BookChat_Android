package com.example.bookchat.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithUser(
    @Embedded
    val chat: ChatEntity,
    @Relation(parentColumn = "sender_id", entityColumn = "id")
    val user: UserEntity?
)