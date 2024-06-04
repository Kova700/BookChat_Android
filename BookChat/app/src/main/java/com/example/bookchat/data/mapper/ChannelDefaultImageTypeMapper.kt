package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.domain.model.ChannelDefaultImageType


fun ChannelDefaultImageTypeNetwork.toDomain(): ChannelDefaultImageType {
	return ChannelDefaultImageType.valueOf(name)
}

fun ChannelDefaultImageType.toNetwork(): ChannelDefaultImageTypeNetwork {
	return ChannelDefaultImageTypeNetwork.valueOf(name)
}