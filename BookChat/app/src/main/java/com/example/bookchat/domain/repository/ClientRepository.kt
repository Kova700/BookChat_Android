package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User
import com.example.bookchat.oauth.external.model.IdToken
import kotlinx.coroutines.flow.Flow

interface ClientRepository {

	fun getClientFlow(): Flow<User>

	suspend fun login(
		idToken: IdToken,
		fcmToken: FCMToken,
		deviceUUID: String,
		isDeviceChangeApproved: Boolean,
	): BookChatToken

	suspend fun signUp(
		idToken: IdToken,
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?,
	)

	suspend fun changeClientProfile(
		newNickname: String,
		userProfile: ByteArray?,
	): User

	suspend fun renewBookChatToken(currentToken: BookChatToken): BookChatToken

	suspend fun getClientProfile(): User
	suspend fun logout()
	suspend fun withdraw()
	suspend fun isDuplicatedUserNickName(nickName: String): Boolean
	suspend fun clear()
}