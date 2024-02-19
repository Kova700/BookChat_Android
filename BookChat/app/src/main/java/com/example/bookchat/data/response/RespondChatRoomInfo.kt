package com.example.bookchat.data.response

import com.example.bookchat.data.local.entity.UserEntity
import com.example.bookchat.utils.UserDefaultProfileImageType
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
    fun toUserEntity() =
        UserEntity(
            id = this.id,
            nickname = this.nickname,
            profileImageUrl = this.profileImageUrl,
            defaultProfileImageType = this.defaultProfileImageType
        )
}