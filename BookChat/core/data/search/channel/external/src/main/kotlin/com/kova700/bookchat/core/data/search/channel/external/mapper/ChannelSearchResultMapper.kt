package com.kova700.bookchat.core.data.search.channel.external.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult

fun ChannelSearchResult.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		roomImageUri = roomImageUri,
		defaultRoomImageType = defaultRoomImageType,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		host = host,
		participants = listOf(host),
		participantAuthorities = mapOf(host.id to ChannelMemberAuthority.HOST),
		lastChat = lastChat,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}
