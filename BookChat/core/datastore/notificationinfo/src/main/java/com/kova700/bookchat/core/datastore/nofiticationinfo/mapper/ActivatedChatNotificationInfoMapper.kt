package com.kova700.bookchat.core.datastore.nofiticationinfo.mapper

import com.kova700.bookchat.core.datastore.nofiticationinfo.model.ActivatedChatNotificationInfoEntity
import com.kova700.core.data.notificationinfo.external.model.ActivatedChatNotificationInfo

fun ActivatedChatNotificationInfo.toEntity(): ActivatedChatNotificationInfoEntity {
	return ActivatedChatNotificationInfoEntity(
		notificationId = notificationId,
		lastTimestamp = lastTimestamp,
	)
}

fun ActivatedChatNotificationInfoEntity.toDomain(): ActivatedChatNotificationInfo {
	return ActivatedChatNotificationInfo(
		notificationId = notificationId,
		lastTimestamp = lastTimestamp,
	)
}

fun Collection<ActivatedChatNotificationInfo>.toEntityInfos(): List<ActivatedChatNotificationInfoEntity> {
	return map { it.toEntity() }
}

fun Collection<ActivatedChatNotificationInfoEntity>.toDomainInfos(): List<ActivatedChatNotificationInfo> {
	return map { it.toDomain() }
}