package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.core.domain.usecase.chat.GetChatUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetClientChannelsFlowUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val userRepository: UserRepository,
	private val getChatUseCase: GetChatUseCase,
) : BaseGetClientChannelUseCase(userRepository, getChatUseCase) {

	operator fun invoke(): Flow<List<Channel>> {
		return channelRepository.getChannelsFlow().map { channels ->
			channels.map { it.attachUserAndChat() }
		}
	}
}