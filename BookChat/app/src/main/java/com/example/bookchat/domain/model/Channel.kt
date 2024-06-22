package com.example.bookchat.domain.model

data class Channel(
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
) {
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

	companion object {
		val DEFAULT = Channel(
			roomId = 0L,
			roomName = "",
			roomSid = "",
			roomMemberCount = 0,
			defaultRoomImageType = ChannelDefaultImageType.ONE
		)
	}
}