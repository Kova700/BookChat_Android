package com.kova700.bookchat.core.network.bookchat.client.model.request

import com.google.gson.annotations.SerializedName

data class RequestChangeUserNickname(
	@SerializedName("nickname")
	val nickname :String
)
