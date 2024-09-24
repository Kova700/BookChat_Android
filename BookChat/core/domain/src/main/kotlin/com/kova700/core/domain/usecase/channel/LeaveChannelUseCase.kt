package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import javax.inject.Inject

class LeaveChannelUseCase @Inject constructor(
	private val chatRepository: ChatRepository,
	private val channelRepository: ChannelRepository,
) {
	suspend operator fun invoke(
		channelId: Long,
	) {
		channelRepository.leaveChannel(channelId)
		chatRepository.deleteChannelAllChat(channelId)
	}
}