package com.kova700.bookchat.core.network.bookchat.chat.model.mapper

import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.network.bookchat.chat.model.response.ChatResponse
import com.kova700.bookchat.core.network.bookchat.chat.model.response.RespondGetChat
import com.kova700.bookchat.core.network.bookchat.user.model.mapper.toDomain

fun ChatResponse.toChat(channelId: Long): Chat {
	return Chat(
		chatId = this.chatId,
		channelId = channelId,
		dispatchTime = this.dispatchTime,
		state = ChatState.SUCCESS,
		message = this.message,
		sender = senderId?.let { User.DEFAULT.copy(id = it) }
	)
}

fun RespondGetChat.toChat(): Chat {
	return Chat(
		chatId = chatId,
		channelId = channelId,
		message = message,
		state = ChatState.SUCCESS,
		dispatchTime = dispatchTime,
		sender = User(
			id = sender.id,
			nickname = sender.nickname,
			profileImageUrl = sender.profileImageUrl,
			defaultProfileImageType = sender.defaultProfileImageType.toDomain()
		)
	)
}
