package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.ActivatedChatNotificationInfo

interface ChattingNotificationInfoRepository {
	suspend fun getShownNotificationInfos(): List<ActivatedChatNotificationInfo>
	suspend fun getNotificationLastTimestamp(notificationId: Int): Long?
	suspend fun updateShownNotificationInfo(notificationId: Int, lastTimestamp: Long)
	suspend fun removeShownNotificationInfo(notificationId: Int)
	suspend fun clearShownNotificationInfos()
}