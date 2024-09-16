package com.kova700.bookchat.core.network.bookchat.search.model.channel.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.search.channel.external.model.ChannelSearchResult
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.mapper.toDomain
import com.kova700.bookchat.core.network.bookchat.search.model.channel.response.ChannelSearchResponse

suspend fun ChannelSearchResponse.toChannelSearchResult(
	getUser: suspend (Long) -> User,
): ChannelSearchResult {
	return ChannelSearchResult(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomImageUri = roomImageUri,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomTags = tags.split(","),
		roomCapacity = roomSize,
		host = host,
		lastChat = this.getLastChat(
			getUser = getUser
		),
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUri,
		isBanned = isBanned,
		isEntered = isEntered
	)
}

fun ChannelSearchResult.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomImageUri = roomImageUri,
		roomMemberCount = roomMemberCount,
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
		isBanned = isBanned,
	)
}
