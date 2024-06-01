package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.BookChatToken
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.model.IdToken
import com.example.bookchat.domain.model.ReadingTaste
import com.example.bookchat.domain.model.User

interface ClientRepository {
	suspend fun signIn(
		approveChangingDevice: Boolean = false,
	)

	suspend fun renewBookChatToken(): BookChatToken?

	suspend fun signUp(
		nickname: String,
		readingTastes: List<ReadingTaste>,
		userProfile: ByteArray?,
	)

	suspend fun getClientProfile(): User
	suspend fun signOut(needAServer: Boolean = false)
	suspend fun withdraw()
	suspend fun isDuplicatedUserNickName(nickName: String): Boolean
	suspend fun renewFCMToken(fcmToken: FCMToken)
	fun getCachedIdToken(): IdToken
	fun saveIdToken(token: IdToken)
}