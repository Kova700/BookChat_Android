package com.kova700.bookchat.core.data.user.external.repository

import com.kova700.bookchat.core.data.user.external.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
	suspend fun getUser(userId: Long): User
	fun getUsersFlow(): Flow<Map<Long, User>>
	suspend fun upsertAllUsers(users: List<User>)
	suspend fun upsertUser(user: User)
	suspend fun clear()
}