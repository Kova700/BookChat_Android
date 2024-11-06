package com.kova700.bookchat.core.remoteconfig.model

data class RemoteConfigValues(
	val isServerEnabled: Boolean,
	val isServerUnderMaintenance: Boolean,
	val serverDownNoticeMessage: String,
	val serverUnderMaintenanceNoticeMessage: String
)