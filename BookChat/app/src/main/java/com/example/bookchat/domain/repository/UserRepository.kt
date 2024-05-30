package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.User

interface UserRepository {
	suspend fun getUser(userId: Long): User
	suspend fun upsertAllUsers(users: List<User>)
}