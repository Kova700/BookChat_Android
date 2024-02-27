package com.example.bookchat.domain.model

data class Channel(
	val roomId: Long,
	val roomName: String,
	val roomSid: String,
	val roomMemberCount: Long,
	val defaultRoomImageType: Int, //개선 필요
	val roomImageUri: String? = null,
	val lastChat: Chat? = null,
	val hostId: Long? = null,
	val subHostIds: List<Long>? = null,
	val guestIds: List<Long>? = null,
	val roomTags: List<String>? = null,
	val roomCapacity: Int? = null,
	val bookTitle: String? = null,
	val bookAuthors: List<String>? = null,
	val bookCoverImageUrl: String? = null,
) {
	companion object {
		val DEFAULT = Channel(
			roomId = 0L,
			roomName = "",
			roomSid = "",
			roomMemberCount = 0,
			defaultRoomImageType = 0,
		)
	}
}

fun Channel.participantIds() =
	mutableListOf<Long>().apply {
		hostId?.let { add(it) }
		subHostIds?.let { addAll(it) }
		guestIds?.let { addAll(it) }
	}.toList()

