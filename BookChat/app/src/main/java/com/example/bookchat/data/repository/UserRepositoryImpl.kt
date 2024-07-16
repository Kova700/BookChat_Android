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
	private val userDao: UserDAO,
) : UserRepository {

	/** 로컬에 있는 유저 우선적으로 쿼리 */
	override suspend fun getUser(userId: Long): User {
		val offlineData = userDao.getUser(userId)?.toUser()
		if (offlineData != null) return offlineData

		val response = runCatching { bookChatApi.getUser(userId).toUser() }
		if (response.isFailure) return User.Default.copy(nickname = "알 수 없음") // 탈퇴한 유저 혹은 잘못된 요청

		userDao.upsertUser(response.getOrThrow().toUserEntity())
		return response.getOrThrow()
	}

	override suspend fun upsertAllUsers(users: List<User>) {
		userDao.upsertAllUsers(users.toUserEntity())
	}

	override suspend fun clear() {
		userDao.deleteAll()
	}

}