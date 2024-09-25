package com.kova700.bookchat.feature.channellist.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.feature.channellist.model.ChannelListItem

fun ChannelListItem.ChannelItem.toChannel(): Channel {
	return Channel(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		isExploded = isExploded,
		isBanned = isBanned,
		participants = participants,
		participantAuthorities = participantAuthorities,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
	)
}

fun Channel.toChannelListItem(isSwiped: Boolean): ChannelListItem.ChannelItem {
	return ChannelListItem.ChannelItem(
		roomId = roomId,
		roomName = roomName,
		roomSid = roomSid,
		roomMemberCount = roomMemberCount,
		defaultRoomImageType = defaultRoomImageType,
		isExistNewChat = isExistNewChat,
		notificationFlag = notificationFlag,
		topPinNum = topPinNum,
		isExploded = isExploded,
		isBanned = isBanned,
		participants = participants,
		participantAuthorities = participantAuthorities,
		roomImageUri = roomImageUri,
		lastChat = lastChat,
		roomTags = roomTags,
		roomCapacity = roomCapacity,
		bookTitle = bookTitle,
		bookAuthors = bookAuthors,
		bookCoverImageUrl = bookCoverImageUrl,
		isSwiped = isSwiped
	)
}

fun List<Channel>.toChannelListItem(isSwipedMap: Map<Long, Boolean>): List<ChannelListItem> {
	val groupedItems = mutableListOf<ChannelListItem>()
	if (this.isEmpty()) return groupedItems

	groupedItems.add(ChannelListItem.Header)
	groupedItems.addAll(
		this.map {
			it.toChannelListItem(isSwipedMap[it.roomId] ?: false)
		}
	)
	return groupedItems
}