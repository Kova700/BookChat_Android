package com.kova700.bookchat.core.network.bookchat.client.model.request

import com.kova700.bookchat.core.network.bookchat.client.model.both.ReadingTasteNetwork
import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.network.bookchat.client.model.both.OAuth2ProviderNetwork
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