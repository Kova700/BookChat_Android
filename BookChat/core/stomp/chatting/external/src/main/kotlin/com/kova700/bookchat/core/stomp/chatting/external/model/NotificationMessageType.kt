package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NotificationMessageType {
	@SerialName("NOTICE_ENTER")
	NOTICE_ENTER,

	@SerialName("NOTICE_EXIT")
	NOTICE_EXIT,

	@SerialName("NOTICE_HOST_EXIT")
	NOTICE_HOST_EXIT,

	@SerialName("NOTICE_HOST_DELEGATE")
	NOTICE_HOST_DELEGATE,

	@SerialName("NOTICE_KICK")
	NOTICE_KICK,

	@SerialName("NOTICE_SUB_HOST_DISMISS")
	NOTICE_SUB_HOST_DISMISS,

	@SerialName("NOTICE_SUB_HOST_DELEGATE")
	NOTICE_SUB_HOST_DELEGATE
}