package com.kova700.bookchat.core.data.user.external.repository

import com.kova700.bookchat.core.data.user.external.model.User

interface UserRepository {
	suspend fun getUser(userId: Long): User
	suspend fun upsertAllUsers(users: List<User>)
	suspend fun clear()
}