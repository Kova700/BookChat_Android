package com.kova700.bookchat.core.data.channel.external.model

import com.kova700.bookchat.core.data.user.external.model.User

data class ChannelInfo(
	val roomCapacity: Int,
	val roomTags: List<String>,
	val roomName: String,
	val bookTitle: String,
	val bookCoverImageUrl: String,
	val bookAuthors: List<String>,
	val roomHost: User,
	val roomSubHostList: List<User>?,
	val roomGuestList: List<User>?,
) {
	val participants
		get() = mutableListOf<User>().apply {
			add(roomHost)
			roomSubHostList?.let { addAll(it) }
			roomGuestList?.let { addAll(it) }
		}.toList()

	val participantIds
		get() = mutableListOf<Long>().apply {
			add(roomHost.id)
			roomSubHostList?.let { addAll(it.map(User::id)) }
			roomGuestList?.let { addAll(it.map(User::id)) }
		}.toList()

	val participantAuthorities
		get() = mutableMapOf<Long, ChannelMemberAuthority>().apply {
			this[roomHost.id] = ChannelMemberAuthority.HOST
			roomSubHostList?.let { list ->
				list.map { user -> this[user.id] = ChannelMemberAuthority.SUB_HOST }
			}
			roomGuestList?.let { list ->
				list.map { user -> this[user.id] = ChannelMemberAuthority.GUEST }
			}
		}
}