package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.google.gson.annotations.SerializedName
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain

//TODO : Host 정보가 필요함
//  val id: Long,
//	val nickname: String,
//	val profileImageUrl: String?,
//	val defaultProfileImageType: UserDefaultProfileType
// 채팅방 목록 꾹 눌러서 뜨는 dialog에서 나가기 누르면 경고 띄워야하는데 방장 유무를 몰라서 빨간글씨 띄울지 말지를 모름

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

	fun getLastChat(): Chat? {
		if (lastChatId == null) return null
		return Chat(
			chatId = lastChatId,
			channelId = roomId,
			message = lastChatContent!!,
			dispatchTime = lastChatDispatchTime!!,
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