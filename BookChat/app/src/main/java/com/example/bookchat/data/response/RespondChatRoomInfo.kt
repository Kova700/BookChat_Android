package com.example.bookchat.data.response

import com.example.bookchat.domain.model.User
import com.example.bookchat.domain.model.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName

// TODO : 채팅방 제목이 추가되어야함 (채팅방 제목 바뀌면 알 수가 없음)
data class RespondChatRoomInfo(
	@SerializedName("roomSize")
	val roomCapacity: Int,
	@SerializedName("roomTags")
	val roomTags: List<String>,
	@SerializedName("bookTitle")
	val bookTitle: String,
	@SerializedName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
	@SerializedName("bookAuthors")
	val bookAuthors: List<String>,
	@SerializedName("roomHost")
	val roomHost: ChatRoomUser,
	@SerializedName("roomSubHostList")
	val roomSubHostList: List<ChatRoomUser>?,
	@SerializedName("roomGuestList")
	val roomGuestList: List<ChatRoomUser>?
)

//TODO : 개선 필요
data class ChatRoomUser(
	@SerializedName("id")
	val id: Long,
	@SerializedName("nickname")
	val nickname: String,
	@SerializedName("profileImageUrl")
	val profileImageUrl: String,
	@SerializedName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileImageType
) {
	fun toUser() =
		User(
			id = this.id,
			nickname = this.nickname,
			profileImageUrl = this.profileImageUrl,
			defaultProfileImageType = this.defaultProfileImageType
		)
}