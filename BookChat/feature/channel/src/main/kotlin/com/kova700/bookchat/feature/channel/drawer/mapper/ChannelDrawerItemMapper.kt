package com.kova700.bookchat.feature.channel.drawer.mapper

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.feature.channel.drawer.model.ChannelDrawerItem

fun Channel.toDrawerItems(client: User): List<ChannelDrawerItem> {
	val items = mutableListOf<ChannelDrawerItem>()
	items.add(
		ChannelDrawerItem.Header(
			roomName = roomName,
			bookTitle = bookTitle,
			bookCoverImageUrl = bookCoverImageUrl,
			bookAuthors = bookAuthorsString
		)
	)

	participants?.let { users ->
		items.addAll(
			users.map { user ->
				ChannelDrawerItem.UserItem(
					id = user.id,
					nickname = user.nickname,
					profileImageUrl = user.profileImageUrl,
					defaultProfileImageType = user.defaultProfileImageType,
					authority = participantAuthorities?.get(user.id) ?: ChannelMemberAuthority.GUEST,
					isClientItem = user.id == client.id
				)
			})
	}

	return items.sortedWith(compareBy {
		when (it) {
			is ChannelDrawerItem.Header -> 0
			is ChannelDrawerItem.UserItem -> when (it.isClientItem) {
				true -> 1
				false -> when (it.authority) {
					ChannelMemberAuthority.HOST -> 2
					ChannelMemberAuthority.SUB_HOST -> 3
					ChannelMemberAuthority.GUEST -> 4
				}
			}
		}
	})

}

fun ChannelDrawerItem.UserItem.toUser(): User {
	return User(
		id = id,
		nickname = nickname,
		profileImageUrl = profileImageUrl,
		defaultProfileImageType = defaultProfileImageType,
	)
}