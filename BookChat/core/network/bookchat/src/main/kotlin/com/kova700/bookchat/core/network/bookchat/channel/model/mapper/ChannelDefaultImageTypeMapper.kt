package com.kova700.bookchat.core.network.bookchat.channel.model.mapper

import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork

fun ChannelDefaultImageTypeNetwork.toDomain(): ChannelDefaultImageType {
	return ChannelDefaultImageType.valueOf(name)
}

fun ChannelDefaultImageType.toNetwork(): ChannelDefaultImageTypeNetwork {
	return ChannelDefaultImageTypeNetwork.valueOf(name)
}