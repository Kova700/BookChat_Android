package com.kova700.bookchat.core.database.chatting.external.channel.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.database.chatting.external.channel.model.ChannelEntity

fun Channel.toChannelEntity(): ChannelEntity {
	return ChannelEntity(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		isNotificationOn = notificationFlag,
		topPinNum = topPinNum,
		isBanned = isBanned,
		isExploded = isExploded,
		roomImageUri = roomImageUri,
		lastChatId = lastChat?.chatId,
		hostId = host?.id,
		participantIds = participantIds,
		participantAuthorities = participantAuthorities,
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
		notificationFlag = isNotificationOn,
		topPinNum = topPinNum,
		isBanned = isBanned,
		isExploded = isExploded,
		roomImageUri = roomImageUri,
		lastChat = lastChatId?.let {
			Chat.DEFAULT.copy(
				chatId = it,
				channelId = roomId,
			)
		},
		lastReadChatId = lastReadChatId,
		host = hostId?.let { User.DEFAULT.copy(id = it) },
		participants = participantIds?.map { User.DEFAULT.copy(id = it) },
		participantAuthorities = participantAuthorities,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}