package com.kova700.bookchat.core.data.search.channel.external.model

import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User

data class ChannelSearchResult(
	val roomId: Long,
	val roomName: String,
	val roomSid: String,
	val roomMemberCount: Int,
	val roomCapacity: Int,
	val defaultRoomImageType: ChannelDefaultImageType,
	val roomImageUri: String?,
	val lastChat: Chat?,
	val host: User,
	val roomTags: List<String>,
	val bookTitle: String,
	val bookAuthors: List<String>,
	val bookCoverImageUrl: String,
) {

	val bookAuthorsString
		get() = bookAuthors.joinToString(",")

	val tagsString
		get() = roomTags.joinToString(" ") { "#$it" }

	companion object {
		val DEFAULT = ChannelSearchResult(
			roomId = 0L,
			roomName = "",
			roomSid = "",
			roomMemberCount = 0,
			roomCapacity = 100,
			defaultRoomImageType = ChannelDefaultImageType.ONE,
			roomImageUri = null,
			lastChat = null,
			host = User.Default,
			roomTags = emptyList(),
			bookTitle = "",
			bookAuthors = emptyList(),
			bookCoverImageUrl = "",
		)
	}
}