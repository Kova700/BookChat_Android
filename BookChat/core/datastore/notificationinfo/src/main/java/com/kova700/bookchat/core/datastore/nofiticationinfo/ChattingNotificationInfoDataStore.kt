package com.kova700.bookchat.core.datastore.nofiticationinfo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kova700.bookchat.core.datastore.datastore.clearData
import com.kova700.bookchat.core.datastore.datastore.getDataFlow
import com.kova700.bookchat.core.datastore.datastore.setData
import com.kova700.bookchat.core.datastore.nofiticationinfo.mapper.toDomainInfos
import com.kova700.bookchat.core.datastore.nofiticationinfo.mapper.toEntityInfos
import com.kova700.bookchat.core.datastore.nofiticationinfo.model.ActivatedChatNotificationInfoEntity
import com.kova700.core.data.notificationinfo.external.model.ActivatedChatNotificationInfo
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChattingNotificationInfoDataStore @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) {
	private val notificationIdKey = stringPreferencesKey(CHATTING_NOTIFICATION_KEY)

	suspend fun getShownNotificationInfos(): List<ActivatedChatNotificationInfo> {
		val notificationIdsString = dataStore.getDataFlow(notificationIdKey).firstOrNull()
		if (notificationIdsString.isNullOrBlank()) return emptyList()
		return Json.decodeFromString<List<ActivatedChatNotificationInfoEntity>>(notificationIdsString)
			.toDomainInfos()
	}

	suspend fun getNotificationLastTimestamp(notificationId: Int): Long? {
		return getShownNotificationInfos()
			.firstOrNull { it.notificationId == notificationId }
			?.lastTimestamp
	}

	suspend fun updateShownNotificationInfo(notificationId: Int, lastTimestamp: Long) {
		val notificationInfos = getShownNotificationInfos()
			.associateByTo(mutableMapOf()) { it.notificationId }

		val previousInfo = notificationInfos[notificationId]
		val currentInfo = ActivatedChatNotificationInfo(notificationId, lastTimestamp)
		if (previousInfo != null && previousInfo.lastTimestamp > currentInfo.lastTimestamp) return
		notificationInfos[notificationId] = currentInfo
		dataStore.setData(
			notificationIdKey,
			Json.encodeToString(notificationInfos.values.toEntityInfos())
		)
	}

	suspend fun removeShownNotificationInfo(notificationId: Int) {
		val notificationInfos = getShownNotificationInfos()
			.filter { it.notificationId != notificationId }
		dataStore.setData(notificationIdKey, Json.encodeToString(notificationInfos.toEntityInfos()))
	}

	suspend fun clear() {
		dataStore.clearData(notificationIdKey)
	}

	companion object {
		private const val CHATTING_NOTIFICATION_KEY = "CHATTING_NOTIFICATION_KEY"
	}
}