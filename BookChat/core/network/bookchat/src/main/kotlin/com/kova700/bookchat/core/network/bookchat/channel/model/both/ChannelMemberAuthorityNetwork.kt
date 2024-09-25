package com.kova700.bookchat.core.network.bookchat.channel.model.both

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChannelMemberAuthorityNetwork {
	@SerialName("HOST")
	HOST,

	@SerialName("SUBHOST")
	SUB_HOST,

	@SerialName("GUEST")
	GUEST
}