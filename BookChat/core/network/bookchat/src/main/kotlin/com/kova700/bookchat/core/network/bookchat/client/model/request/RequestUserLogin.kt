package com.kova700.bookchat.core.network.bookchat.client.model.request

import com.kova700.bookchat.core.network.bookchat.client.model.both.OAuth2ProviderNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUserLogin(
	@SerialName("fcmToken")
	val fcmToken: String,
	@SerialName("deviceToken")
	val deviceToken: String,
	@SerialName("approveChangingDevice")
	val isDeviceChangeApproved: Boolean,
	@SerialName("oauth2Provider")
	val oauth2Provider: OAuth2ProviderNetwork,
)