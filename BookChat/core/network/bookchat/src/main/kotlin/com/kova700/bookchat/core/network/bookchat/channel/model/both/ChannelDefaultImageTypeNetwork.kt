package com.kova700.bookchat.core.network.bookchat.channel.model.both

import com.kova700.bookchat.core.network.network.converter.EnumAsIntSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = ChannelDefaultImageTypeNetworkSerializer::class)
enum class ChannelDefaultImageTypeNetwork(val code: Int) {
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7)
}

class ChannelDefaultImageTypeNetworkSerializer :
	KSerializer<ChannelDefaultImageTypeNetwork> by EnumAsIntSerializer(
		codeGetter = { enum: ChannelDefaultImageTypeNetwork -> enum.code },
		enumGetter = { code: Int ->
			ChannelDefaultImageTypeNetwork.entries.first { it.code == code }
		}
	)