package com.example.bookchat.data.repository

import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.repository.FCMTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//TODO : FirebaseMessaging.getInstance() 의존성 주입으로 수정
//TODO : FCM 패키지로 이동
class FCMTokenRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
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
			FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
				if (task.isSuccessful.not()) continuation.resumeWithException(
					task.exception ?: Exception("Failed to retrieve FCM token")
				)
				else continuation.resume(FCMToken(task.result))
			}
		}
	}

	override suspend fun expireFCMToken() {
		suspendCancellableCoroutine<Unit> { continuation ->
			FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
				if (task.isSuccessful.not()) continuation.resumeWithException(
					task.exception ?: Exception("Failed to delete FCM token")
				)
				else continuation.resume(Unit)
			}
		}
	}

}