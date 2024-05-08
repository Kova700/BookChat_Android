package com.example.bookchat.ui.channel.model.drawer

import com.example.bookchat.domain.model.UserDefaultProfileType

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
		val bookAuthors: List<String>?
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
		val defaultProfileImageType: UserDefaultProfileType
	) : ChannelDrawerItem

	companion object {
		const val DRAWER_HEADER_ITEM_STABLE_ID = -1L
	}
}