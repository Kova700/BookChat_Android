package com.example.bookchat.data.network.model.request

import com.google.gson.annotations.SerializedName

data class RequestChangeUserNickname(
	@SerializedName("nickname")
	val nickname :String
)
