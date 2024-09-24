package com.kova700.core.domain.usecase.channel

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import javax.inject.Inject

class EnterChannelMemberUseCase @Inject constructor(
	private val userRepository: UserRepository,
	private val channelRepository: ChannelRepository,
) {
	suspend operator fun invoke(
		channelId: Long,
		targetUserId: Long,
	) {
		val targetUser = userRepository.getUser(targetUserId)
		channelRepository.enterChannelMember(
			channelId = channelId,
			targetUser = targetUser
		)
	}
}