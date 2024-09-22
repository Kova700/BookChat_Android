package com.kova700.bookchat.core.network.bookchat.client.model.request

import com.kova700.bookchat.core.network.bookchat.client.model.both.OAuth2ProviderNetwork
import com.kova700.bookchat.core.network.bookchat.client.model.both.ReadingTasteNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Random

@Serializable
data class RequestUserSignUp(
	@SerialName("oauth2Provider")
	val oauth2Provider: OAuth2ProviderNetwork,
	@SerialName("nickname")
	var nickname: String,
	@SerialName("readingTastes")
	var readingTastes: List<ReadingTasteNetwork>,
	@SerialName("defaultProfileImageType")
	val defaultProfileImageType: Int = Random().nextInt(5) + 1,
)