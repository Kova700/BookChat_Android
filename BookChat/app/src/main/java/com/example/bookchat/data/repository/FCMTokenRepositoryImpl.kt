package com.example.bookchat.data.repository

import com.example.bookchat.domain.model.FCMToken
import com.example.bookchat.domain.repository.BookChatTokenRepository
import com.example.bookchat.domain.repository.FCMTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FCMTokenRepositoryImpl @Inject constructor(
	private val bookChatTokenRepository: BookChatTokenRepository,
) : FCMTokenRepository {

	override suspend fun renewFCMToken(fcmToken: FCMToken) {
		if (bookChatTokenRepository.isBookChatTokenExist().not()) return
		//TODO : 매번 로그인 시에 호출하여 서버에 등록된 FCM 토큰 덮어쓰기 (API 연결 필요)
		// 가장 최근 기기로 알람가게 구현
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