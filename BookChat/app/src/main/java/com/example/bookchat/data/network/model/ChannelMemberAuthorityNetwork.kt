package com.example.bookchat.data.network.model

import com.google.gson.annotations.SerializedName

enum class ChannelMemberAuthorityNetwork {
	@SerializedName("HOST") HOST,
	@SerializedName("SUBHOST ") SUB_HOST,
	@SerializedName("GUEST") GUEST
}