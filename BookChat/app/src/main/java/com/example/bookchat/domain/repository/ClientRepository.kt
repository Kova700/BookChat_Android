package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ClientRepository {

	fun getClientFlow(): Flow<User>

	suspend fun isSignedIn(): Boolean

	suspend fun renewBookChatToken(): BookChatToken

	suspend fun changeClientProfile(
		newNickname: String,
		userProfile: ByteArray?,
	): User

	suspend fun getClientProfile(): User
	suspend fun signOut()
	suspend fun withdraw()
	suspend fun isDuplicatedUserNickName(nickName: String): Boolean
	suspend fun renewFCMToken(fcmToken: FCMToken)
	fun getCachedIdToken(): IdToken
	fun saveIdToken(token: IdToken)

	suspend fun clear()
}