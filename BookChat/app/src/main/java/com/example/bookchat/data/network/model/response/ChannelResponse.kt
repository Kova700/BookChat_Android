package com.example.bookchat.data.network.model.response

import com.example.bookchat.data.mapper.toUserDefaultProfileType
import com.example.bookchat.data.network.model.ChannelDefaultImageTypeNetwork
import com.example.bookchat.data.network.model.UserDefaultProfileTypeNetwork
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
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
	val roomMemberCount: Long,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: ChannelDefaultImageTypeNetwork,
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
	val lastChat
		get() = lastChatId?.let {
			Chat(
				chatId = it,
				chatRoomId = roomId,
				message = lastChatContent!!,
				chatType = ChatType.UNKNOWN,
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