package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import javax.inject.Inject

class SyncChannelChatsUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository
) {

	suspend operator fun invoke(channelId: Long) {
		val newestChat = chatRepository.syncChats(channelId).firstOrNull() ?: return
		channelRepository.updateChannelLastChatIfValid(channelId, newestChat.chatId)
	}
}