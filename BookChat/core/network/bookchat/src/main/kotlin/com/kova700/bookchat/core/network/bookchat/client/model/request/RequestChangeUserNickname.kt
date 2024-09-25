package com.kova700.bookchat.core.network.bookchat.client.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestChangeUserNickname(
	@SerialName("nickname")
	val nickname: String,
)
