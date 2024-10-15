package com.kova700.bookchat.core.network.bookchat.search.model.channel.response

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO : [NotUrgent] tags 타입 List로 반환해서 받으면 좋을 듯
@Serializable
data class ChannelSearchResponse(
	@SerialName("roomId")
	val roomId: Long,
	@SerialName("roomName")
	val roomName: String,
	@SerialName("roomSid")
	val roomSid: String,
	@SerialName("bookTitle")
	val bookTitle: String,
	@SerialName("bookAuthors")
	val bookAuthors: List<String>,
	@SerialName("bookCoverImageUri")
	val bookCoverImageUri: String,
	@SerialName("roomMemberCount")
	val roomMemberCount: Int,
	@SerialName("roomSize")
	val roomSize: Int,
	@SerialName("hostId")
	val hostId: Long,
	@SerialName("hostName")
	val hostName: String,
	@SerialName("hostDefaultProfileImageType")
	val hostDefaultProfileImageType: UserDefaultProfileTypeNetwork,
	@SerialName("hostProfileImageUri")
	val hostProfileImageUri: String,
	@SerialName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerialName("tags")
	val tags: String,
	@SerialName("roomImageUri")
	val roomImageUri: String? = null,
	@SerialName("lastChatSenderId")
	val lastChatSenderId: Long? = null,
	@SerialName("lastChatId")
	val lastChatId: Long? = null,
	@SerialName("lastChatMessage")
	val lastChatMessage: String? = null,
	@SerialName("lastChatDispatchTime")
	val lastChatDispatchTime: String? = null,
) {
	val host
		get() = User(
			id = hostId,
			nickname = hostName,
			profileImageUrl = hostProfileImageUri,
			defaultProfileImageType = hostDefaultProfileImageType.toDomain(),
		)

	val lastChat: Chat?
		get() = lastChatId?.let {
			Chat(
				chatId = it,
				channelId = roomId,
				message = lastChatMessage!!,
				dispatchTime = lastChatDispatchTime!!,
				sender = lastChatSenderId?.let { senderId -> User.DEFAULT.copy(id = senderId) }
			)
		}

}