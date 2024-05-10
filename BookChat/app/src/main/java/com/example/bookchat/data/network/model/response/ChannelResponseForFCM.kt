package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.google.gson.annotations.SerializedName

data class ChannelResponseForFCM(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Long,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerializedName("roomImageUri")
	val roomImageUri: String,
)