package com.example.bookchat.data.repository

import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.repository.FCMTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FCMTokenRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : FCMTokenRepository {

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		bookChatApi.renewFcmToken(fcmToken.text)
	}

	override suspend fun getFCMToken(): FCMToken {
		return suspendCancellableCoroutine<Result<String>> { continuation ->
			FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
				if (task.isSuccessful.not()) return@addOnCompleteListener
				continuation.resume(Result.success(task.result))
			}
		}.map { FCMToken(it) }.getOrElse { e -> throw e }
	}
}