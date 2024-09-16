package com.kova700.bookchat.core.network.bookchat.channel.model.both

import com.google.gson.annotations.SerializedName

enum class ChannelMemberAuthorityNetwork {
	@SerializedName("HOST") HOST,
	@SerializedName("SUBHOST ") SUB_HOST,
	@SerializedName("GUEST") GUEST
}