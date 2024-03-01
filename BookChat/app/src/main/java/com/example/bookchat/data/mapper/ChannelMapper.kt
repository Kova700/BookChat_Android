package com.example.bookchat.data.mapper

import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.combined.ChannelWithInfo
import com.example.bookchat.data.response.ChannelResponse
import com.example.bookchat.data.response.getLastChat
import com.example.bookchat.domain.model.Channel

fun ChannelResponse.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
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
		defaultRoomImageType = defaultRoomImageType,
		roomImageUri = roomImageUri,
		lastChat = getLastChat(), // sender 정보 없음
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

fun List<ChannelResponse>.toChannelEntity() = this.map { it.toChannelEntity() }
fun List<ChannelWithInfo>.toChannel() = this.map { it.toChannel() }
