package com.kova700.bookchat.core.network.bookchat.channel.model.mapper

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelMemberAuthorityNetwork

fun ChannelMemberAuthority.toNetwork(): ChannelMemberAuthorityNetwork {
	return ChannelMemberAuthorityNetwork.valueOf(name)
}

fun ChannelMemberAuthorityNetwork.toDomain(): ChannelMemberAuthority {
	return ChannelMemberAuthority.valueOf(name)
}