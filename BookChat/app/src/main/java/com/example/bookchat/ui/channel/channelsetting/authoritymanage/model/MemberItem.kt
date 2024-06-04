package com.example.bookchat.ui.channel.channelsetting.authoritymanage.model

import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.UserDefaultProfileType

data class MemberItem(
	val id: Long,
	val nickname: String,
	val profileImageUrl: String?,
	val defaultProfileImageType: UserDefaultProfileType,
	val authority: ChannelMemberAuthority,
	val isSelected: Boolean,
) {
	val isTargetUserSubHost
		get() = authority == ChannelMemberAuthority.SUB_HOST
}