package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetChatsFlowUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository
) {
	operator fun invoke(
		initFlag: Boolean,
		channelId: Long,
	): Flow<List<Chat>> {
		return channelRepository.getChannelFlow(channelId)
			.combine(
				chatRepository.getChatsFlow(
					initFlag = initFlag,
					channelId = channelId
				)
			) { channel, chats ->
				val participantsMap = channel.participants?.associateBy { it.id }
				chats.map { chat -> chat.copy(sender = participantsMap?.get(chat.sender?.id)) }
			}
	}
}