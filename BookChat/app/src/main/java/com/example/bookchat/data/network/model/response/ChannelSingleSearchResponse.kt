package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.google.gson.annotations.SerializedName

//TODO : lastChatId 추가되면 상당히 편할 듯
data class ChannelSingleSearchResponse(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Int,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerializedName("roomImageUri")
	val roomImageUri: String,
)