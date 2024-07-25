package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.FCMToken

interface FCMTokenRepository {
	suspend fun renewFCMToken(fcmToken: FCMToken)
	suspend fun getNewFCMToken(): FCMToken
	suspend fun getFCMToken(): FCMToken
	suspend fun expireFCMToken()
}