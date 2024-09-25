package com.kova700.core.data.notificationinfo.external.repository

import com.kova700.core.data.notificationinfo.external.model.ActivatedChatNotificationInfo

interface ChattingNotificationInfoRepository {
	suspend fun getShownNotificationInfos(): List<ActivatedChatNotificationInfo>
	suspend fun getNotificationLastTimestamp(notificationId: Int): Long?
	suspend fun updateShownNotificationInfo(notificationId: Int, lastTimestamp: Long)
	suspend fun removeShownNotificationInfo(notificationId: Int)
	suspend fun clear()
}