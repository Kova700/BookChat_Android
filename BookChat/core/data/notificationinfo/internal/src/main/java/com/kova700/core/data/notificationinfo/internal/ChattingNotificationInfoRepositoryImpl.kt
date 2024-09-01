package com.kova700.core.data.notificationinfo.internal

import com.kova700.bookchat.core.datastore.nofiticationinfo.ChattingNotificationInfoDataStore
import com.kova700.core.data.notificationinfo.external.model.ActivatedChatNotificationInfo
import com.kova700.core.data.notificationinfo.external.repository.ChattingNotificationInfoRepository
import javax.inject.Inject

class ChattingNotificationInfoRepositoryImpl @Inject constructor(
	private val dataStore: ChattingNotificationInfoDataStore,
) : ChattingNotificationInfoRepository {

	override suspend fun getShownNotificationInfos(): List<ActivatedChatNotificationInfo> {
		return dataStore.getShownNotificationInfos()
	}

	override suspend fun getNotificationLastTimestamp(notificationId: Int): Long? {
		return dataStore.getNotificationLastTimestamp(notificationId)
	}

	override suspend fun updateShownNotificationInfo(notificationId: Int, lastTimestamp: Long) {
		dataStore.updateShownNotificationInfo(notificationId, lastTimestamp)
	}

	override suspend fun removeShownNotificationInfo(notificationId: Int) {
		dataStore.removeShownNotificationInfo(notificationId)
	}

	override suspend fun clear() {
		dataStore.clear()
	}

}