package com.kova700.bookchat.core.network.bookchat.channel.model.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ChannelResponse
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ChannelSingleSearchResponse

fun ChannelResponse.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		host = host,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

//TODO : [NotUrgent] lastChatId 추가되면 상당히 편할 듯
fun ChannelSingleSearchResponse.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
	)
}