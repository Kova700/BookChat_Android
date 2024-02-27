package com.example.bookchat.data

import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.UserDefaultProfileImageType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

//TODO : lastChatContent도 포함되면 이용하기 좋을 듯
//TODO : hostId도 포함되면 이용하기 좋을 듯
//TODO : tags 타입 List로 반환해서 받으면 좋을 듯
data class WholeChatRoomListItem(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("bookTitle")
	val bookTitle: String,
	@SerializedName("bookAuthors")
	val bookAuthors: List<String>,
	@SerializedName("bookCoverImageUri")
	val bookCoverImageUri: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Long,
	@SerializedName("roomSize")
	val roomSize: Int,
	@SerializedName("hostName")
	val hostName: String,
	@SerializedName("hostDefaultProfileImageType")
	val hostDefaultProfileImageType: UserDefaultProfileImageType,
	@SerializedName("hostProfileImageUri")
	val hostProfileImageUri: String,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: Int,
	@SerializedName("tags")
	val tags: String,
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
	@SerializedName("lastChatId")
	val lastChatId: Long? = null,
	@SerializedName("lastActiveTime")
	val lastActiveTime: String? = null
) : Serializable {
	fun getBookAuthorsString() = bookAuthors.joinToString(",")
	private fun getTagsList(): List<String> {
		return tags.split(" ")
	}

	fun toChannelEntity(): ChannelEntity {
		return ChannelEntity(
			roomId = roomId,
			roomName = roomName,
			roomSid = roomSid,
			roomMemberCount = roomMemberCount,
			defaultRoomImageType = defaultRoomImageType,
			roomImageUri = roomImageUri,
			lastChatId = lastChatId
		)
	}

	fun toChannel(): Channel {
		return Channel(
			roomId = roomId,
			roomName = roomName,
			roomSid = roomSid,
			roomImageUri = roomImageUri,
			roomMemberCount = roomMemberCount,
			defaultRoomImageType = defaultRoomImageType,
			roomTags = getTagsList(),
			roomCapacity = roomSize,
			bookTitle = bookTitle,
			bookAuthors = bookAuthors,
			bookCoverImageUrl = bookCoverImageUri,
			)
	}
}