package com.example.bookchat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bookchat.data.datastore.clearData
import com.example.bookchat.data.datastore.getDataFlow
import com.example.bookchat.data.datastore.setData
import com.example.bookchat.domain.model.ActivatedChatNotificationInfo
import com.example.bookchat.domain.repository.ChattingNotificationInfoRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ChattingNotificationInfoRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val gson: Gson,
) : ChattingNotificationInfoRepository {
	private val notificationIdKey = stringPreferencesKey(CHATTING_NOTIFICATION_KEY)

	override suspend fun getShownNotificationInfos(): List<ActivatedChatNotificationInfo> {
		val notificationIdsString = dataStore.getDataFlow(notificationIdKey).firstOrNull()
		if (notificationIdsString.isNullOrBlank()) return emptyList()
		return gson.fromJson(notificationIdsString, Array<ActivatedChatNotificationInfo>::class.java)
			.toList()
	}

	override suspend fun getNotificationLastTimestamp(notificationId: Int): Long? {
		return getShownNotificationInfos()
			.firstOrNull { it.notificationId == notificationId }
			?.lastTimestamp
	}

	override suspend fun updateShownNotificationInfo(notificationId: Int, lastTimestamp: Long) {
		val notificationInfos = getShownNotificationInfos()
			.associateBy { it.notificationId }
			.toMutableMap()

		val previousInfo = notificationInfos[notificationId]
		val currentInfo = ActivatedChatNotificationInfo(notificationId, lastTimestamp)
		if (previousInfo != null && previousInfo.lastTimestamp > currentInfo.lastTimestamp) return

		notificationInfos[notificationId] = currentInfo
		dataStore.setData(notificationIdKey, gson.toJson(notificationInfos.toList()))
	}

	override suspend fun removeShownNotificationInfo(notificationId: Int) {
		val notificationInfos = getShownNotificationInfos()
			.filter { it.notificationId != notificationId }
		dataStore.setData(notificationIdKey, gson.toJson(notificationInfos))
	}

	override suspend fun clearShownNotificationInfos() {
		dataStore.clearData(notificationIdKey)
	}

	companion object {
		private const val CHATTING_NOTIFICATION_KEY = "CHATTING_NOTIFICATION_KEY"
	}
}