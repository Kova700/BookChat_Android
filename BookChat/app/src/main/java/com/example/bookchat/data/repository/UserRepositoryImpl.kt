package com.example.bookchat.data.repository

import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.mapper.toUserEntity
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.model.participantIds
import com.example.bookchat.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
	private val userDao: UserDAO
) : UserRepository {

	override suspend fun getUsers(userIds: List<Long>): List<User> {
		return userDao.getUserList(userIds).toUser()
	}

	override suspend fun getUser(userId: Long): User {
		return userDao.getUser(userId).toUser()
	}

	override suspend fun upsertAllUsers(users: List<User>) {
		userDao.upsertAllUsers(users.toUserEntity())
	}

	override suspend fun getChannelUsers(channel: Channel): List<User> {
		return getUsers(channel.participantIds())
	}

}