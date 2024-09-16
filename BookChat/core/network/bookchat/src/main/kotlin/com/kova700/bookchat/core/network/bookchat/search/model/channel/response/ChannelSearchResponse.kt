package com.kova700.bookchat.core.network.bookchat.search.model.channel.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain

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
	val isEntered: Boolean,
	@SerializedName("isBanned")
	val isBanned: Boolean,
) {
	val host
		get() = User(
			id = hostId,
			nickname = hostName,
			profileImageUrl = hostProfileImageUri,
			defaultProfileImageType = hostDefaultProfileImageType.toDomain(),
		)

	suspend fun getLastChat(
		getUser: suspend (Long) -> User,
	): Chat? {
		if (lastChatId == null) return null

		return Chat(
			chatId = lastChatId,
			channelId = roomId,
			message = lastChatMessage!!,
			dispatchTime = lastChatDispatchTime!!,
			sender = lastChatSenderId?.let { getUser(it) }
		)
	}

}