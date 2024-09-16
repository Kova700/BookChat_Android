package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
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