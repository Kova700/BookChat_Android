package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import javax.inject.Inject

class GetNewestChatsUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository
) {
	suspend operator fun invoke(
		channelId: Long,
	): List<Chat> {
		val chats = chatRepository.getNewestChats(channelId)
		chats.firstOrNull()?.chatId?.let {
			channelRepository.updateChannelLastChat(channelId, it)
		}
		return chats
	}
}