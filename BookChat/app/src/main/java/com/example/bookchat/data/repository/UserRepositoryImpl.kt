package com.example.bookchat.data.repository

import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.database.model.ChatRoomEntity
import com.example.bookchat.data.database.model.UserEntity
import com.example.bookchat.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
	private val userDao: UserDAO
) : UserRepository {

	override suspend fun getUserList(userIdList: List<Long>): List<UserEntity> {
		return userDao.getUserList(userIdList)
	}

	override suspend fun getUser(userId: Long): UserEntity {
		return userDao.getUser(userId)
	}

	override suspend fun insertOrUpdateAllUser(users: List<UserEntity>) {
		userDao.insertOrUpdateAllUser(users)
	}

	override suspend fun getChatRoomUserList(chatRoomEntity: ChatRoomEntity): List<UserEntity> {
		return getUserList(mutableListOf<Long>().apply {
			chatRoomEntity.hostId?.let { add(it) }
			chatRoomEntity.subHostIds?.let { addAll(it) }
			chatRoomEntity.guestIds?.let { addAll(it) }
		})
	}

}