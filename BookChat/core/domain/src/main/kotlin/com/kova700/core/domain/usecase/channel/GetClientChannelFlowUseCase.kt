package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.core.domain.usecase.chat.GetChatUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetClientChannelFlowUseCase @Inject constructor(
	private val userRepository: UserRepository,
	private val channelRepository: ChannelRepository,
	private val getChatUseCase: GetChatUseCase,
) : BaseGetClientChannelUseCase(userRepository, getChatUseCase) {

	suspend operator fun invoke(
		channelId: Long,
	): Flow<Channel> {
		return channelRepository.getChannelFlow(channelId)
			.map { channel -> channel.attachUserAndChat() }
	}
}