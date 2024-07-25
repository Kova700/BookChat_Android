package com.example.bookchat.fcm.repository.internal

import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.fcm.repository.external.FCMTokenRepository
import com.example.bookchat.fcm.repository.external.model.FCMToken
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FCMTokenRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val firebaseMessaging: FirebaseMessaging,
) : FCMTokenRepository {

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		bookChatApi.renewFcmToken(fcmToken.text)
	}

	override suspend fun getNewFCMToken(): FCMToken {
		expireFCMToken()
		return getFCMToken()
	}

	override suspend fun getFCMToken(): FCMToken {
		return suspendCancellableCoroutine<FCMToken> { continuation ->
			firebaseMessaging.token.addOnCompleteListener { task ->
				if (task.isSuccessful.not()) continuation.resumeWithException(
					task.exception ?: Exception("Failed to retrieve FCM token")
				)
				else continuation.resume(FCMToken(task.result))
			}
		}
	}

	override suspend fun expireFCMToken() {
		suspendCancellableCoroutine<Unit> { continuation ->
			firebaseMessaging.deleteToken().addOnCompleteListener { task ->
				if (task.isSuccessful.not()) continuation.resumeWithException(
					task.exception ?: Exception("Failed to delete FCM token")
				)
				else continuation.resume(Unit)
			}
		}
	}

}