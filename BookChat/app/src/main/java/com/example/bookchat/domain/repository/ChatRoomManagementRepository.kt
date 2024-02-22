package com.example.bookchat.domain.repository

interface ChatRoomManagementRepository {
	suspend fun enterChatRoom(roomId: Long)
	suspend fun leaveChatRoom(roomId: Long)
	suspend fun getChatRoomInfo(roomId: Long)
}