package com.example.bookchat.domain.model

/** isEntered 유무 확인을 위해서 Channel 클래스와 구분 */
data class ChannelSearchResult(
	val roomId: Long,
	val roomName: String,
	val roomSid: String,
	val roomMemberCount: Int,
	val roomCapacity: Int,
	val defaultRoomImageType: ChannelDefaultImageType,
	val isBanned: Boolean,
	val isEntered: Boolean,
	val roomImageUri: String?,
	val lastChat: Chat?,
	val host: User,
	val roomTags: List<String>,
	val bookTitle: String,
	val bookAuthors: List<String>,
	val bookCoverImageUrl: String,
) {
	val isFull
		get() = roomMemberCount >= roomCapacity

	val bookAuthorsString
		get() = bookAuthors.joinToString(",")

	val tagsString
		get() = roomTags.joinToString(" ") { "# $it" }

	companion object {
		val DEFAULT = ChannelSearchResult(
			roomId = 0L,
			roomName = "",
			roomSid = "",
			roomMemberCount = 0,
			roomCapacity = 100,
			defaultRoomImageType = ChannelDefaultImageType.ONE,
			isBanned = false,
			isEntered = false,
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