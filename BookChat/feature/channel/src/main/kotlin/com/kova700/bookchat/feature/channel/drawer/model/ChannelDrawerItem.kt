package com.kova700.bookchat.feature.channel.drawer.model

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType

sealed interface ChannelDrawerItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Header -> DRAWER_HEADER_ITEM_STABLE_ID
			is UserItem -> id
		}
	}

	data class Header(
		val roomName: String,
		val bookTitle: String?,
		val bookCoverImageUrl: String?,
		val bookAuthors: String?,
	) : ChannelDrawerItem {
		companion object {
			val DEFAULT = Header(
				roomName = "",
				bookTitle = null,
				bookCoverImageUrl = null,
				bookAuthors = null
			)
		}
	}

	data class UserItem(
		val id: Long,
		val nickname: String,
		val profileImageUrl: String?,
		val defaultProfileImageType: UserDefaultProfileType,
		val authority: ChannelMemberAuthority,
		val isClientItem: Boolean,
	) : ChannelDrawerItem {
		val isTargetUserHost
			get() = authority == ChannelMemberAuthority.HOST

		val isTargetUserSubHost
			get() = authority == ChannelMemberAuthority.SUB_HOST
	}

	companion object {
		const val DRAWER_HEADER_ITEM_STABLE_ID = -1L
	}
}