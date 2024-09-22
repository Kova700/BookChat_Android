package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.model

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType

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