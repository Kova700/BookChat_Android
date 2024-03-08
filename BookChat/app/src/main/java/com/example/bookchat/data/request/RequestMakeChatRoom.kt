package com.example.bookchat.data.request

import com.google.gson.annotations.SerializedName

data class RequestMakeChatRoom(
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSize")
	val roomSize: Int,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: Int,
	@SerializedName("hashTags")
	val hashTags: List<String>,
	@SerializedName("bookRequest")
	val bookRequest: BookRequest
)
