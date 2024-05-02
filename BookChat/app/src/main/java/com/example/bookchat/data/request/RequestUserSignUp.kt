package com.example.bookchat.data.request

import com.example.bookchat.data.model.OAuth2ProviderNetwork
import com.example.bookchat.data.model.ReadingTasteNetwork
import com.google.gson.annotations.SerializedName
import java.util.*

data class RequestUserSignUp(
	@SerializedName("oauth2Provider")
	val oauth2Provider: OAuth2ProviderNetwork,
	@SerializedName("nickname")
	var nickname: String,
	@SerializedName("readingTastes")
	var readingTastes: List<ReadingTasteNetwork>,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: Int = Random().nextInt(5) + 1,
)