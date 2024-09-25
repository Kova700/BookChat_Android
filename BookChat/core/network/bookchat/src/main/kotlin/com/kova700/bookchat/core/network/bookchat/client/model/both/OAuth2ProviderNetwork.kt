package com.kova700.bookchat.core.network.bookchat.client.model.both

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OAuth2ProviderNetwork {
	@SerialName("google")
	GOOGLE,

	@SerialName("kakao")
	KAKAO
}