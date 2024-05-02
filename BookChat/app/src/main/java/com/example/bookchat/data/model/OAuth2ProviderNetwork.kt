package com.example.bookchat.data.model

import com.google.gson.annotations.SerializedName

enum class OAuth2ProviderNetwork {
	@SerializedName("google")
	GOOGLE,
	@SerializedName("kakao")
	KAKAO
}