package com.example.bookchat.domain.repository

import com.example.bookchat.data.database.model.ChatRoomEntity
import com.example.bookchat.data.database.model.UserEntity

interface UserRepository {
	suspend fun getUserList(userIdList: List<Long>): List<UserEntity>
	suspend fun getChatRoomUserList(chatRoomEntity: ChatRoomEntity): List<UserEntity>
	suspend fun getUser(userId: Long): UserEntity
	suspend fun insertOrUpdateAllUser(users: List<UserEntity>)
}