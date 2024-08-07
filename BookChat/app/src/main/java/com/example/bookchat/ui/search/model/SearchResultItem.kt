package com.example.bookchat.ui.search.model

import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User

sealed interface SearchResultItem {
	fun getCategoryId(): String {
		return when (this) {
			BookHeader -> BOOK_HEADER_ITEM_STABLE_ID
			is BookDummy -> hashCode().toString()
			BookEmpty -> BOOK_EMPTY_ITEM_STABLE_ID
			is BookItem -> isbn
			ChannelHeader -> CHANNEL_HEADER_ITEM_STABLE_ID
			ChannelEmpty -> CHANNEL_EMPTY_ITEM_STABLE_ID
			is ChannelItem -> roomId.toString()
		}
	}

	object BookHeader : SearchResultItem
	data class BookItem(
		val isbn: String,
		var title: String,
		val authors: List<String>,
		val publisher: String,
		var publishAt: String,
		val bookCoverImageUrl: String,
	) : SearchResultItem

	data class BookDummy(val id: Int) : SearchResultItem
	object BookEmpty : SearchResultItem

	object ChannelHeader : SearchResultItem
	data class ChannelItem(
		val roomId: Long,
		val roomName: String,
		val roomSid: String,
		val roomMemberCount: Int,
		val defaultRoomImageType: ChannelDefaultImageType,
		val roomImageUri: String?,
		val lastChat: Chat?,
		val host: User,
		val tagsString: String,
		val roomCapacity: Int,
	) : SearchResultItem

	object ChannelEmpty : SearchResultItem

	companion object {
		const val BOOK_HEADER_ITEM_STABLE_ID = "BOOK_HEADER_ITEM_STABLE_ID"
		const val CHANNEL_HEADER_ITEM_STABLE_ID = "CHANNEL_HEADER_ITEM_STABLE_ID"
		const val BOOK_EMPTY_ITEM_STABLE_ID = "BOOK_EMPTY_ITEM_STABLE_ID"
		const val CHANNEL_EMPTY_ITEM_STABLE_ID = "CHANNEL_EMPTY_ITEM_STABLE_ID"
	}
}