package com.kova700.bookchat.core.network.bookchat.channel.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** {"errorCode":"4000501","message":"이미 참여한 참여자 입니다." } */
@Serializable
data class FailResponseBody(
	@SerialName("errorCode")
	val errorCode: Int,
	@SerialName("message")
	val message: String,
)