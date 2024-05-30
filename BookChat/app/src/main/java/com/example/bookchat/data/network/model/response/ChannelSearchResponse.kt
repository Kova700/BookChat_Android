package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.mapper.getChatType
import com.example.bookchat.data.mapper.toUserDefaultProfileType
import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User
import com.google.gson.annotations.SerializedName

//TODO : tags 타입 List로 반환해서 받으면 좋을 듯
data class ChannelSearchResponse(
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
	val roomMemberCount: Int,
	@SerializedName("roomSize")
	val roomSize: Int,
	@SerializedName("hostId")
	val hostId: Long,
	@SerializedName("hostName")
	val hostName: String,
	@SerializedName("hostDefaultProfileImageType")
	val hostDefaultProfileImageType: UserDefaultProfileTypeNetwork,
	@SerializedName("hostProfileImageUri")
	val hostProfileImageUri: String,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerializedName("tags")
	val tags: String,
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
	@SerializedName("lastChatSenderId")
	val lastChatSenderId: Long? = null,
	@SerializedName("lastChatId")
	val lastChatId: Long? = null,
	@SerializedName("lastChatMessage")
	val lastChatMessage: String? = null,
	@SerializedName("lastChatDispatchTime")
	val lastChatDispatchTime: String? = null,
	@SerializedName("isEntered")
	val isEntered :Boolean, //TODO : Channel말고 isEntered 필드 반영하는 새로운 data class만들어야할듯?
	@SerializedName("isBanned")
	val isBanned :Boolean
) {
	val host
		get() = User(
			id = hostId,
			nickname = hostName,
			profileImageUrl = hostProfileImageUri,
			defaultProfileImageType = hostDefaultProfileImageType.toUserDefaultProfileType(),
		)

	suspend fun getLastChat(
		clientId: Long,
		getUser: suspend (Long) -> User
	): Chat? {
		if (lastChatId == null) return null

		return Chat(
			chatId = lastChatId,
			chatRoomId = roomId,
			message = lastChatMessage!!,
			chatType = getChatType(
				senderId = lastChatSenderId,
				clientId = clientId
			),
			dispatchTime = lastChatDispatchTime!!,
			sender = lastChatSenderId?.let { getUser(it) }
		)
	}

}