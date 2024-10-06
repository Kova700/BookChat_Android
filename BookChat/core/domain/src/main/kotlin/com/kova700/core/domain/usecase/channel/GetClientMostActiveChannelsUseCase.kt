package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import javax.inject.Inject

class GetClientMostActiveChannelsUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val userRepository: UserRepository,
	private val chatRepository: ChatRepository,
) {
	suspend operator fun invoke(isOfflineOnly: Boolean = false) {
		val channels = channelRepository.getMostActiveChannels(isOfflineOnly = isOfflineOnly)
		channels.forEach { channel ->
			channel.lastChat?.let { chat ->
				chatRepository.insertChat(chat)
				chat.sender?.let { user -> userRepository.upsertUser(user) }
			}
		}
	}
}