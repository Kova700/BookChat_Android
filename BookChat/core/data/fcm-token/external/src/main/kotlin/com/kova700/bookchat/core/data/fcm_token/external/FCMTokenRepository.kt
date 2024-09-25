package com.kova700.bookchat.core.data.fcm_token.external

import com.kova700.bookchat.core.data.fcm_token.external.model.FCMToken

interface FCMTokenRepository {
	suspend fun renewFCMToken(fcmToken: FCMToken)
	suspend fun getNewFCMToken(): FCMToken
	suspend fun getFCMToken(): FCMToken
	suspend fun expireFCMToken()
}