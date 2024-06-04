package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ChannelMemberAuthorityNetwork
import com.example.bookchat.domain.model.ChannelMemberAuthority

fun ChannelMemberAuthority.toNetwork(): ChannelMemberAuthorityNetwork {
	return ChannelMemberAuthorityNetwork.valueOf(name)
}

fun ChannelMemberAuthorityNetwork.toDomain(): ChannelMemberAuthority {
	return ChannelMemberAuthority.valueOf(name)
}