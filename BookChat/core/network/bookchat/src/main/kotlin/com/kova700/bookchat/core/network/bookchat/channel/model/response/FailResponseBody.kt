package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.google.gson.annotations.SerializedName

/** {"errorCode":"4000501","message":"이미 참여한 참여자 입니다." } */
data class FailResponseBody(
	@SerializedName("errorCode")
	val errorCode: Int,
	@SerializedName("message")
	val message: String,
)