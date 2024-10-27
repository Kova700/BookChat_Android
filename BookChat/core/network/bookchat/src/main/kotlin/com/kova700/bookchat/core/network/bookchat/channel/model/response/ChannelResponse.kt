package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelResponse(
	@SerialName("roomId")
	val roomId: Long,
	@SerialName("roomName")
	val roomName: String,
	@SerialName("roomSid")
	val roomSid: String,
	@SerialName("roomMemberCount")
	val roomMemberCount: Int,
	@SerialName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerialName("hostId")
	val hostId: Long,
	@SerialName("hostNickname")
	val hostNickname: String,
	@SerialName("hostProfileImageUrl")
	val hostProfileImageUrl: String?,
	@SerialName("hostDefaultProfileImageType")
	val hostDefaultProfileImageType: UserDefaultProfileTypeNetwork,
	@SerialName("bookTitle")
	val bookTitle: String,
	@SerialName("bookAuthors")
	val bookAuthors: List<String>,
	@SerialName("bookCoverImageUrl")
	val bookCoverImageUrl: String?,
	@SerialName("lastChatId")
	val lastChatId: Long? = null,
	@SerialName("lastChatContent")
	val lastChatContent: String? = null,
	@SerialName("lastChatDispatchTime")
	val lastChatDispatchTime: String? = null,
	@SerialName("senderId")
	val senderId: Long? = null,
	@SerialName("senderNickname")
	val senderNickname: String? = null,
	@SerialName("senderProfileImageUrl")
	val senderProfileImageUrl: String? = null,
	@SerialName("senderDefaultProfileImageType")
	val senderDefaultProfileImageType: UserDefaultProfileTypeNetwork? = null,
	@SerialName("roomImageUri")
	val roomImageUri: String? = null,
) {
	val host: User
		get() = User(
			id = hostId,
			nickname = hostNickname,
			profileImageUrl = hostProfileImageUrl,
			defaultProfileImageType = hostDefaultProfileImageType.toDomain()
		)

	val lastChat: Chat?
		get() {
			if (lastChatId == null) return null
			return Chat(
				chatId = lastChatId,
				channelId = roomId,
				message = lastChatContent!!,
				dispatchTime = lastChatDispatchTime!!,
				state = ChatState.SUCCESS,
				sender = senderId?.let { id ->
					User(
						id = id,
						nickname = senderNickname!!,
						profileImageUrl = senderProfileImageUrl,
						defaultProfileImageType = senderDefaultProfileImageType!!.toDomain()
					)
				}
			)
		}
}