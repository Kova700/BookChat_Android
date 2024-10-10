package com.kova700.bookchat.core.data.fcm_token.internal

import com.google.firebase.messaging.FirebaseMessaging
import com.kova700.bookchat.core.data.fcm_token.external.FCMTokenRepository
import com.kova700.bookchat.core.data.fcm_token.external.model.FCMToken
import com.kova700.bookchat.core.network.bookchat.client.ClientApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FCMTokenRepositoryImpl @Inject constructor(
	private val clientApi: ClientApi,
	private val firebaseMessaging: FirebaseMessaging,
) : FCMTokenRepository {

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		clientApi.renewFcmToken(fcmToken.text)
	}

	override suspend fun getFCMToken(): FCMToken {
		return suspendCancellableCoroutine<FCMToken> { continuation ->
			firebaseMessaging.token.addOnCompleteListener { task ->
				if (task.isSuccessful) continuation.resume(FCMToken(text = task.result))
				else continuation.resumeWithException(
					task.exception ?: Exception("Failed to retrieve FCM token")
				)
			}
		}
	}

	override suspend fun expireFCMToken() {
		suspendCancellableCoroutine<Unit> { continuation ->
			firebaseMessaging.deleteToken().addOnCompleteListener { task ->
				if (task.isSuccessful) continuation.resume(Unit)
				else continuation.resumeWithException(
					task.exception ?: Exception("Failed to delete FCM token")
				)
			}
		}
	}

}