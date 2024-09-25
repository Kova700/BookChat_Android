package com.kova700.core.domain.usecase.chat

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
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