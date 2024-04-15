package com.example.bookchat.data.response

import com.example.bookchat.data.mapper.toUserDefaultProfileType
import com.example.bookchat.data.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.User
import com.google.gson.annotations.SerializedName

data class ResponseChannelInfo(
	@SerializedName("roomSize")
	val roomCapacity: Int,
	@SerializedName("roomTags")
	val roomTags: List<String>,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("bookTitle")
	val bookTitle: String,
	@SerializedName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
	@SerializedName("bookAuthors")
	val bookAuthors: List<String>,
	@SerializedName("roomHost")
	val roomHost: ChannelUser,
	@SerializedName("roomSubHostList")
	val roomSubHostList: List<ChannelUser>?,
	@SerializedName("roomGuestList")
	val roomGuestList: List<ChannelUser>?
) {
	val participants
		get() = mutableListOf<User>().apply {
			add(roomHost.toUser())
			roomSubHostList?.let { addAll(it.map(ChannelUser::toUser)) }
			roomGuestList?.let { addAll(it.map(ChannelUser::toUser)) }
		}.toList()
}

//TODO : UserResponse와 프로퍼티명 통일하여 개선 필요
data class ChannelUser(
	@SerializedName("id")
	val id: Long,
	@SerializedName("nickname")
	val nickname: String,
	@SerializedName("profileImageUrl")
	val profileImageUrl: String,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileTypeNetwork
) {
	fun toUser() =
		User(
			id = this.id,
			nickname = this.nickname,
			profileImageUrl = this.profileImageUrl,
			defaultProfileImageType = this.defaultProfileImageType.toUserDefaultProfileType()
		)
}