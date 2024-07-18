package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ClientRepository {

	fun getClientFlow(): Flow<User>

	suspend fun changeClientProfile(
		newNickname: String,
		userProfile: ByteArray?,
	): User

	suspend fun getClientProfile(): User
	suspend fun logout()
	suspend fun withdraw()
	suspend fun isDuplicatedUserNickName(nickName: String): Boolean
	suspend fun clear()
}