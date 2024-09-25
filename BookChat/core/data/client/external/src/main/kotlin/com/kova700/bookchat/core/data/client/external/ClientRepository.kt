package com.kova700.bookchat.core.data.client.external

import com.kova700.bookchat.core.data.bookchat_token.external.model.BookChatToken
import com.kova700.bookchat.core.data.client.external.model.ReadingTaste
import com.kova700.bookchat.core.data.fcm_token.external.model.FCMToken
import com.kova700.bookchat.core.data.oauth.external.model.IdToken
import com.kova700.bookchat.core.data.user.external.model.User
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