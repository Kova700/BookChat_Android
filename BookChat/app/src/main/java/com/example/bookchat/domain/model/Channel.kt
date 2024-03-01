package com.example.bookchat.domain.model

data class Channel(
	val roomId: Long,
	val roomName: String,
	val roomSid: String,
	val roomMemberCount: Long,
	val defaultRoomImageType: Int, //개선 필요
	val notificationFlag :Boolean = true,
	val topPinNum :Int = 0,
	val roomImageUri: String? = null,
	val lastChat: Chat? = null,
	val host: User? = null,
	val subHosts: List<User>? = null,
	val guests: List<User>? = null,
	val roomTags: List<String>? = null,
	val roomCapacity: Int? = null,
	val bookTitle: String? = null,
	val bookAuthors: List<String>? = null,
	val bookCoverImageUrl: String? = null,
)

fun Channel.participants() =
	mutableListOf<User>().apply {
		host?.let { add(it) }
		subHosts?.let { addAll(it) }
		guests?.let { addAll(it) }
	}.toList()

fun Channel.participantIds() =
	mutableListOf<Long>().apply {
		host?.id?.let { add(it) }
		subHosts?.map { it.id }?.let { addAll(it) }
		guests?.map { it.id }?.let { addAll(it) }
	}.toList()

