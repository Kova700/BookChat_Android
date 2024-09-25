package com.kova700.bookchat.core.data.channel.external.model

import java.util.Random

enum class ChannelDefaultImageType(val num: Int) {
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7);

	companion object {
		fun getNewRandomType(now: ChannelDefaultImageType = ONE): ChannelDefaultImageType {
			var new = Random().nextInt(7) + 1
			while (new == now.num) {
				new = Random().nextInt(7) + 1
			}
			return entries.first { it.num == new }
		}
	}
}
