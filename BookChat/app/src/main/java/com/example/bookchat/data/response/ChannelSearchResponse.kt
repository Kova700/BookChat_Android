package com.example.bookchat.data.response

import com.example.bookchat.domain.model.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

//TODO : tags 타입 List로 반환해서 받으면 좋을 듯
data class ChannelSearchResponse(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("bookTitle")
	val bookTitle: String,
	@SerializedName("bookAuthors")
	val bookAuthors: List<String>,
	@SerializedName("bookCoverImageUri")
	val bookCoverImageUri: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Long,
	@SerializedName("roomSize")
	val roomSize: Int,
	@SerializedName("hostId")
	val hostId: Long,
	@SerializedName("hostName")
	val hostName: String,
	@SerializedName("hostDefaultProfileImageType")
	val hostDefaultProfileImageType: UserDefaultProfileImageType,
	@SerializedName("hostProfileImageUri")
	val hostProfileImageUri: String,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: Int,
	@SerializedName("tags")
	val tags: String,
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
	@SerializedName("lastChatSenderId")
	val lastChatSenderId: Long? = null,
	@SerializedName("lastChatId")
	val lastChatId: Long? = null,
	@SerializedName("lastChatMessage")
	val lastChatMessage: String? = null,
	@SerializedName("lastChatDispatchTime")
	val lastChatDispatchTime: String? = null
)
