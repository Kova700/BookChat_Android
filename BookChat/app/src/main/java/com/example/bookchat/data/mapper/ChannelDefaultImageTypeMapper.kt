package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.domain.model.ChannelDefaultImageType


fun ChannelDefaultImageTypeNetwork.toChannelDefaultImageType(): ChannelDefaultImageType {
	return when (this) {
		ChannelDefaultImageTypeNetwork.ONE -> ChannelDefaultImageType.ONE
		ChannelDefaultImageTypeNetwork.TWO -> ChannelDefaultImageType.TWO
		ChannelDefaultImageTypeNetwork.THREE -> ChannelDefaultImageType.THREE
		ChannelDefaultImageTypeNetwork.FOUR -> ChannelDefaultImageType.FOUR
		ChannelDefaultImageTypeNetwork.FIVE -> ChannelDefaultImageType.FIVE
		ChannelDefaultImageTypeNetwork.SIX -> ChannelDefaultImageType.SIX
		ChannelDefaultImageTypeNetwork.SEVEN -> ChannelDefaultImageType.SEVEN
	}
}

fun ChannelDefaultImageType.toChannelDefaultImageTypeNetwork(): ChannelDefaultImageTypeNetwork {
	return when (this) {
		ChannelDefaultImageType.ONE -> ChannelDefaultImageTypeNetwork.ONE
		ChannelDefaultImageType.TWO -> ChannelDefaultImageTypeNetwork.TWO
		ChannelDefaultImageType.THREE -> ChannelDefaultImageTypeNetwork.THREE
		ChannelDefaultImageType.FOUR -> ChannelDefaultImageTypeNetwork.FOUR
		ChannelDefaultImageType.FIVE -> ChannelDefaultImageTypeNetwork.FIVE
		ChannelDefaultImageType.SIX -> ChannelDefaultImageTypeNetwork.SIX
		ChannelDefaultImageType.SEVEN -> ChannelDefaultImageTypeNetwork.SEVEN
	}
}