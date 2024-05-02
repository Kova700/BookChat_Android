package com.example.bookchat.ui.channelList.mapper

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.channelList.model.ChannelListItem

fun ChannelListItem.ChannelItem.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		host = host,
		subHosts = subHosts,
		guests = guests,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

fun Channel.toChannelListItem(): ChannelListItem.ChannelItem {
	return ChannelListItem.ChannelItem(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		host = host,
		subHosts = subHosts,
		guests = guests,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl
	)
}