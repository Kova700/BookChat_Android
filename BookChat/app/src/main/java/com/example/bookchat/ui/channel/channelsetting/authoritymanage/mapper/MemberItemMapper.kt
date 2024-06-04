package com.example.bookchat.ui.channel.channelsetting.authoritymanage.mapper

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.model.MemberItem

fun Channel.toMemberItems(
	searchKeyword: String,
	selectedMemberId: Long?,
	filterTypes: List<ChannelMemberAuthority>,
): List<MemberItem> {
	val items = mutableListOf<MemberItem>()

	participants?.let { users ->
		items.addAll(
			users.filter { user ->
				(filterTypes.contains(participantAuthorities?.get(user.id)).not())
								&& if (searchKeyword.isBlank()) true else user.nickname.contains(searchKeyword)
			}.map { user ->
				MemberItem(
					id = user.id,
					nickname = user.nickname,
					profileImageUrl = user.profileImageUrl,
					defaultProfileImageType = user.defaultProfileImageType,
					authority = participantAuthorities?.get(user.id) ?: ChannelMemberAuthority.GUEST,
					isSelected = user.id == selectedMemberId
				)
			}
		)
	}

	return items.sortedWith(compareBy {
		when (it.authority) {
			ChannelMemberAuthority.SUB_HOST -> 1
			ChannelMemberAuthority.GUEST -> 2
			ChannelMemberAuthority.HOST -> Int.MAX_VALUE
		}
	})
}