package com.kova700.bookchat.core.network.bookchat.chat.model.response

import com.kova700.bookchat.core.network.bookchat.user.model.both.UserDefaultProfileTypeNetwork
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RespondGetChat(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("chatRoomId")
	val channelId: Long,
	@SerialName("message")
	val message: String,
	@SerialName("dispatchTime")
	val dispatchTime: String,
	@SerialName("sender")
	val sender: ResponseUser,
)

@Serializable
data class ResponseUser(
	@SerialName("id")
	val id: Long,
	@SerialName("nickname")
	val nickname: String,
	@SerialName("profileImageUrl")
	val profileImageUrl: String?,
	@SerialName("defaultProfileImageType")
	val defaultProfileImageType: UserDefaultProfileTypeNetwork,
)