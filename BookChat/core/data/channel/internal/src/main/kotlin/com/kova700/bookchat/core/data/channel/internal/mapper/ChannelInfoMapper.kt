package com.kova700.bookchat.core.data.channel.internal.mapper

import com.kova700.bookchat.core.data.channel.external.model.ChannelInfo
import com.kova700.bookchat.core.network.bookchat.channel.model.response.ResponseChannelInfo

fun ResponseChannelInfo.toDomain(): ChannelInfo {
	return ChannelInfo(
		roomCapacity = roomCapacity,
		roomTags = roomTags,
		roomName = roomName,
		bookTitle = bookTitle,
		bookCoverImageUrl = bookCoverImageUrl,
		bookAuthors = bookAuthors,
		roomHost = roomHost.toUser(),
		roomSubHostList = roomSubHostList?.map { it.toUser() },
		roomGuestList = roomGuestList?.map { it.toUser() },
	)
}