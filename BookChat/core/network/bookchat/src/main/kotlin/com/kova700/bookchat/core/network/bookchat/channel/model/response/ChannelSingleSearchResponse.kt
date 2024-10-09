package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO : lastChatId 추가되면 상당히 편할 듯
//TODO : 여기도 Host, LastChat 필요
@Serializable
data class ChannelSingleSearchResponse(
	@SerialName("roomId")
	val roomId: Long,
	@SerialName("roomName")
	val roomName: String,
	@SerialName("roomSid")
	val roomSid: String,
	@SerialName("roomMemberCount")
	val roomMemberCount: Int,
	@SerialName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerialName("roomImageUri")
	val roomImageUri: String? = null,
)