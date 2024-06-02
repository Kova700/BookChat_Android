package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.mapper.getChatType
import com.example.bookchat.data.mapper.toUserDefaultProfileType
import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User
import com.google.gson.annotations.SerializedName

data class ChannelResponse(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Int,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
	@SerializedName("isBanned")
	val isBanned: Boolean,
	@SerializedName("isExploded")
	val isExploded: Boolean,
	@SerializedName("lastChatId")
	val lastChatId: Long? = null,
	@SerializedName("lastChatContent")
	val lastChatContent: String? = null,
	@SerializedName("lastChatDispatchTime")
	val lastChatDispatchTime: String? = null,
	@SerializedName("senderId")
	val senderId: Long? = null,
	@SerializedName("senderNickname")
	val senderNickname: String? = null,
	@SerializedName("senderProfileImageUrl")
	val senderProfileImageUrl: String? = null,
	@SerializedName("senderDefaultProfileImageType")
	val senderDefaultProfileImageType: UserDefaultProfileTypeNetwork? = null,
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
) {

	fun getLastChat(clientId: Long): Chat? {
		if (lastChatId == null) return null
		return Chat(
			chatId = lastChatId,
			chatRoomId = roomId,
			message = lastChatContent!!,
			chatType = getChatType(
				senderId = senderId,
				clientId = clientId
			),
			dispatchTime = lastChatDispatchTime!!,
			sender = senderId?.let { id ->
				User(
					id = id,
					nickname = senderNickname!!,
					profileImageUrl = senderProfileImageUrl,
					defaultProfileImageType = senderDefaultProfileImageType!!.toUserDefaultProfileType()
				)
			}
		)
	}
}