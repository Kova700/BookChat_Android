package com.kova700.bookchat.core.datastore.nofiticationinfo.model

import kotlinx.serialization.Serializable

@Serializable
data class ActivatedChatNotificationInfoEntity(
	val notificationId: Int,
	val lastTimestamp: Long,
)