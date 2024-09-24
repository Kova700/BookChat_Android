package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import javax.inject.Inject

class GetClientChannelInfoUseCase @Inject constructor(
	private val userRepository: UserRepository,
	private val channelRepository: ChannelRepository,
) {
	suspend operator fun invoke(
		channelId: Long,
	) {
		val channelInfo = channelRepository.getChannelInfo(channelId) ?: return
		userRepository.upsertAllUsers(channelInfo.participants)
	}
}