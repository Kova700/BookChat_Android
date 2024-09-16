package com.kova700.bookchat.core.network.bookchat.client.model.both

import com.google.gson.annotations.SerializedName

enum class OAuth2ProviderNetwork {
	@SerializedName("google")
	GOOGLE,
	@SerializedName("kakao")
	KAKAO
}