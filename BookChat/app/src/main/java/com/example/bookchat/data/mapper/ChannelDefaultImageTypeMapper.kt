package com.example.bookchat.data.mapper

import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.domain.model.ChannelDefaultImageType


fun com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.toChannelDefaultImageType(): ChannelDefaultImageType {
	return when (this) {
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.ONE -> ChannelDefaultImageType.ONE
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.TWO -> ChannelDefaultImageType.TWO
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.THREE -> ChannelDefaultImageType.THREE
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.FOUR -> ChannelDefaultImageType.FOUR
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.FIVE -> ChannelDefaultImageType.FIVE
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.SIX -> ChannelDefaultImageType.SIX
		com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.SEVEN -> ChannelDefaultImageType.SEVEN
	}
}

fun ChannelDefaultImageType.toChannelDefaultImageTypeNetwork(): com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork {
	return when (this) {
		ChannelDefaultImageType.ONE -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.ONE
		ChannelDefaultImageType.TWO -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.TWO
		ChannelDefaultImageType.THREE -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.THREE
		ChannelDefaultImageType.FOUR -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.FOUR
		ChannelDefaultImageType.FIVE -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.FIVE
		ChannelDefaultImageType.SIX -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.SIX
		ChannelDefaultImageType.SEVEN -> com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork.SEVEN
	}
}