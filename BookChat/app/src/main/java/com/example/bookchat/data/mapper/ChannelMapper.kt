package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.combined.ChannelWithInfo
import com.example.bookchat.data.network.model.response.ChannelResponse
import com.example.bookchat.data.network.model.response.ChannelSearchResponse
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User

fun ChannelResponse.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toChannelDefaultImageType(),
		roomImageUri = roomImageUri,
		lastChatId = lastChatId,
	)
}

fun ChannelResponse.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toChannelDefaultImageType(),
		roomImageUri = roomImageUri,
		lastChat = lastChat,
	)
}

fun Channel.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		roomImageUri = roomImageUri,
		lastChatId = lastChat?.chatId,
		hostId = host?.id,
		subHostIds = subHosts?.map { it.id },
		guestIds = guests?.map { it.id },
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl
	)
}

fun ChannelEntity.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		roomImageUri = roomImageUri,
		lastChat = lastChatId?.let { Chat.DEFAULT.copy(chatId = it) }, // sender 정보 없을 수 있음
		host = hostId?.let { User.Default.copy(id = it) }, //ChannelRepository에서 보충 예정
		subHosts = subHostIds?.map { User.Default.copy(id = it) },
		guests = guestIds?.map { User.Default.copy(id = it) },
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

fun ChannelWithInfo.toChannel(): Channel {
	return Channel(
		roomId = channelEntity.roomId,
		roomName = channelEntity.roomName,
		roomSid = channelEntity.roomSid,
		roomMemberCount = channelEntity.roomMemberCount,
		defaultRoomImageType = channelEntity.defaultRoomImageType,
		notificationFlag = channelEntity.notificationFlag,
		topPinNum = channelEntity.topPinNum,
		roomImageUri = channelEntity.roomImageUri,
		lastChat = chatEntity.toChat(), // sender 정보 없을 수 있음
		host = hostUserEntity?.toUser(),
		subHosts = subHostUserEntities?.toUser(),
		guests = guestUserEntities?.toUser(),
		roomTags = channelEntity.roomTags,
		roomCapacity = channelEntity.roomCapacity,
		bookTitle = channelEntity.bookTitle,
		bookAuthors = channelEntity.bookAuthors,
		bookCoverImageUrl = channelEntity.bookCoverImageUrl,
	)
}

fun ChannelSearchResponse.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomImageUri = roomImageUri,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType.toChannelDefaultImageType(),
		roomTags = tags.split(","),
		roomCapacity = roomSize,
		host = host,
		lastChat = lastChat,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUri,
	)
}

fun List<ChannelResponse>.toChannelEntity() =
	this.map { it.toChannelEntity() }

fun List<ChannelEntity>.toChannel() = this.map { it.toChannel() }
