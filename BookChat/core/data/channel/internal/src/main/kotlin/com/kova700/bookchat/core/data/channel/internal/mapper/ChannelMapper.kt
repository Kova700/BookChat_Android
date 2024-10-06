package com.kova700.bookchat.core.data.channel.internal.mapper

import com.kova700.bookchat.core.database.chatting.external.channel.model.ChannelEntity
import com.kova700.bookchat.core.network.bookchat.channel.model.mapper.toDomain
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ChannelResponse

fun ChannelResponse.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toDomain(),
		roomImageUri = roomImageUri,
		lastChatId = lastChatId,
		hostId = hostId,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

fun List<ChannelResponse>.toChannelEntity() = this.map { it.toChannelEntity() }