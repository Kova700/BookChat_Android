package com.example.bookchat.ui.channel.mapper

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.channel.model.drawer.ChannelDrawerItem

fun Channel.toDrawerItems(): List<ChannelDrawerItem> {
	val items = mutableListOf<ChannelDrawerItem>()
	items.add(
		ChannelDrawerItem.Header(
			roomName = roomName,
			bookTitle = bookTitle,
			bookCoverImageUrl = bookCoverImageUrl,
			bookAuthors = bookAuthors
		)
	)
	items.addAll(
		participants.map { user ->
			ChannelDrawerItem.UserItem(
				id = user.id,
				nickname = user.nickname,
				profileImageUrl = user.profileImageUrl,
				defaultProfileImageType = user.defaultProfileImageType
			)
		})
	return items
}