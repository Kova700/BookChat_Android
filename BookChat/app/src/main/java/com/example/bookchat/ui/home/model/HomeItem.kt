package com.example.bookchat.ui.home.model

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.BookShelfState
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User
import java.util.Date

sealed interface HomeItem {
	fun getCategoryId(): Long {
		return when (this) {
			is BookHeader -> BOOK_HEADER_ITEM_STABLE_ID
			is BookItem -> bookShelfId
			is BookDummy -> hashCode().toLong()
			BookEmpty -> BOOK_EMPTY_ITEM_STABLE_ID
			BookRetry -> BOOK_RETRY_ITEM_STABLE_ID
			BookLoading -> BOOK_LOADING_ITEM_STABLE_ID
			is ChannelHeader -> CHANNEL_HEADER_ITEM_STABLE_ID
			is ChannelItem -> roomId
			ChannelEmpty -> CHANNEL_EMPTY_ITEM_STABLE_ID
			ChannelRetry -> CHANNEL_RETRY_ITEM_STABLE_ID
			ChannelLoading -> CHANNEL_LOADING_ITEM_STABLE_ID
		}
	}

	data object BookHeader : HomeItem
	data object BookEmpty : HomeItem
	data object BookRetry : HomeItem
	data object BookLoading : HomeItem
	data class BookDummy(val id: Int) : HomeItem
	data class BookItem(
		val bookShelfId: Long,
		val book: Book,
		val state: BookShelfState,
		val lastUpdatedAt: Date,
	) : HomeItem

	data object ChannelHeader : HomeItem
	data object ChannelEmpty : HomeItem
	data object ChannelRetry : HomeItem
	data object ChannelLoading : HomeItem
	data class ChannelItem(
		val roomId: Long,
		val roomName: String,
		val roomSid: String,
		val roomMemberCount: Int,
		val defaultRoomImageType: ChannelDefaultImageType,
		val notificationFlag: Boolean = true,
		val topPinNum: Int = 0,
		val isBanned: Boolean = false,
		val isExploded: Boolean = false,
		val roomImageUri: String? = null,
		val lastReadChatId: Long? = null,
		val lastChat: Chat? = null,
		val host: User? = null,
		val participants: List<User>? = null,
		val participantAuthorities: Map<Long, ChannelMemberAuthority>? = null,
		val roomTags: List<String>? = null,
		val roomCapacity: Int? = null,
		val bookTitle: String? = null,
		val bookAuthors: List<String>? = null,
		val bookCoverImageUrl: String? = null,
	) : HomeItem{
		val isTopPined
			get() = topPinNum != 0

		val isExistNewChat
			get() = when {
				lastReadChatId == null -> false
				lastChat?.chatId == null -> false
				lastReadChatId < lastChat.chatId -> true
				else -> false
			}

		val isAvailableChannel
			get() = isBanned.not() && isExploded.not()

		val bookAuthorsString
			get() = bookAuthors?.joinToString(",")

		val tagsString
			get() = roomTags?.joinToString(" ") { "# $it" }

		val participantIds
			get() = participants?.map { it.id }

		val subHosts
			get() = participants?.filter {
				participantAuthorities?.get(it.id) == ChannelMemberAuthority.SUB_HOST
			} ?: emptyList()
	}

	companion object {
		const val BOOK_HEADER_ITEM_STABLE_ID = -1L
		const val BOOK_EMPTY_ITEM_STABLE_ID = -2L
		const val BOOK_RETRY_ITEM_STABLE_ID = -3L
		const val BOOK_LOADING_ITEM_STABLE_ID = -4L
		const val CHANNEL_HEADER_ITEM_STABLE_ID = -5L
		const val CHANNEL_EMPTY_ITEM_STABLE_ID = -6L
		const val CHANNEL_RETRY_ITEM_STABLE_ID = -7L
		const val CHANNEL_LOADING_ITEM_STABLE_ID = -8L
	}
}