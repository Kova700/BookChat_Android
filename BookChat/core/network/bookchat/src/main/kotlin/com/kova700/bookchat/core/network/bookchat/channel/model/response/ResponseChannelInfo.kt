package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseChannelInfo(
	@SerialName("roomSize")
	val roomCapacity: Int,
	@SerialName("roomTags")
	val roomTags: List<String>,
	@SerialName("roomName")
	val roomName: String,
	@SerialName("bookTitle")
	val bookTitle: String,
	@SerialName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
	@SerialName("bookAuthors")
	val bookAuthors: List<String>,
	@SerialName("roomHost")
	val roomHost: ChannelUser,
	@SerialName("roomSubHostList")
	val roomSubHostList: List<ChannelUser>?,
	@SerialName("roomGuestList")
	val roomGuestList: List<ChannelUser>?,
) {
	val participants
		get() = mutableListOf<User>().apply {
			add(roomHost.toUser())
			roomSubHostList?.let { addAll(it.map(ChannelUser::toUser)) }
			roomGuestList?.let { addAll(it.map(ChannelUser::toUser)) }
		}.toList()

	val participantIds
		get() = mutableListOf<Long>().apply {
			add(roomHost.id)
			roomSubHostList?.let { user -> addAll(user.map(ChannelUser::id)) }
			roomGuestList?.let { user -> addAll(user.map(ChannelUser::id)) }
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

//TODO : UserResponse와 프로퍼티명 통일하여 개선 필요
@Serializable
data class ChannelUser(
	@SerialName("id")
	val id: Long,
	@SerialName("nickname")
	val nickname: String,
	@SerialName("profileImageUrl")
	val profileImageUrl: String,
	@SerialName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileTypeNetwork,
) {
	fun toUser() =
		User(
			id = this.id,
			nickname = this.nickname,
			profileImageUrl = this.profileImageUrl,
			defaultProfileImageType = this.defaultProfileImageType.toDomain()
		)
}