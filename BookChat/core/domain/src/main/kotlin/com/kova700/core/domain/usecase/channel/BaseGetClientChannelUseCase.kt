package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.core.domain.usecase.chat.GetChatUseCase

open class BaseGetClientChannelUseCase(
	private val userRepository: UserRepository,
	private val getChatUseCase: GetChatUseCase,
) {
	private suspend fun getChat(chatId: Long) = getChatUseCase(chatId)
	private suspend fun getUser(userId: Long) = userRepository.getUser(userId)

	protected suspend fun Channel.attachUserAndChat(): Channel {
		return copy(
			lastChat = lastChat?.chatId?.let { getChat(it) },
			host = host?.id?.let { getUser(it) },
			participants = participantIds?.map { getUser(it) },
		)
	}

}