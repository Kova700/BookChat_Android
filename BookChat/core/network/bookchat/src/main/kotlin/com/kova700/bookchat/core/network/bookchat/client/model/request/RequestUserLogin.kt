package com.kova700.bookchat.core.network.bookchat.client.model.request

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.client.model.both.OAuth2ProviderNetwork

data class RequestUserLogin(
	@SerializedName("fcmToken")
	val fcmToken: String,
	@SerializedName("deviceToken")
	val deviceToken: String,
	@SerializedName("approveChangingDevice")
	val isDeviceChangeApproved: Boolean,
	@SerializedName("oauth2Provider")
	val oauth2Provider: OAuth2ProviderNetwork
)