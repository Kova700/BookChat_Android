package com.kova700.bookchat.core.network.bookchat.channel.model.response

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.channel.model.both.ChannelDefaultImageTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO : Host 정보가 필요함
//  val id: Long,
//	val nickname: String,
//	val profileImageUrl: String?,
//	val defaultProfileImageType: UserDefaultProfileType
// 채팅방 목록 꾹 눌러서 뜨는 dialog에서 나가기 누르면 경고 띄워야하는데 방장 유무를 몰라서 빨간글씨 띄울지 말지를 모름

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

	val lastChat: Chat?
		get() {
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