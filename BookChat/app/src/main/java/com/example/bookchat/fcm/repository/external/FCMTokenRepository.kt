package com.example.bookchat.fcm.repository.external

import com.example.bookchat.fcm.repository.external.model.FCMToken

interface FCMTokenRepository {
	suspend fun renewFCMToken(fcmToken: FCMToken)
	suspend fun getNewFCMToken(): FCMToken
	suspend fun getFCMToken(): FCMToken
	suspend fun expireFCMToken()
}