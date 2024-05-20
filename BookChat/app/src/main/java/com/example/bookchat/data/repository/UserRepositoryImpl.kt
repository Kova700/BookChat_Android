package com.example.bookchat.data.repository

import com.example.bookchat.data.database.dao.UserDAO
import com.example.bookchat.data.mapper.toUser
import com.example.bookchat.data.mapper.toUserEntity
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val userDao: UserDAO
) : UserRepository {

	/** 로컬에 있는 유저 우선적으로 쿼리 */
	override suspend fun getUser(userId: Long): User {
		return userDao.getUser(userId)?.toUser()
			?: bookChatApi.getUser(userId).toUser().also {
				userDao.upsertUser(it.toUserEntity())
			}
	}

	override suspend fun upsertAllUsers(users: List<User>) {
		userDao.upsertAllUsers(users.toUserEntity())
	}

}