package com.example.bookchat.domain.repository

import com.example.bookchat.data.database.model.ChatEntity

interface ChatRoomManagementRepository {
	suspend fun enterChatRoom(roomId: Long)
	suspend fun leaveChatRoom(roomId: Long)
	suspend fun getChatRoomInfo(roomId: Long)
	suspend fun updateMemberCount(roomId: Long, offset: Int)
	suspend fun updateLastChat(chat: ChatEntity)
}