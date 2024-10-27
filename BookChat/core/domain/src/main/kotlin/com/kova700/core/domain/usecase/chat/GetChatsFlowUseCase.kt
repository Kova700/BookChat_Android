package com.kova700.core.domain.usecase.chat

import com.kova700.bookchat.core.data.channel.external.repository.ChannelRepository
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.core.data.chat.external.repository.ChatRepository
import com.kova700.bookchat.core.data.client.external.ClientRepository
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetChatsFlowUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
) : BaseGetChatUseCase(userRepository, clientRepository) {
	operator fun invoke(
		initFlag: Boolean,
		channelId: Long,
	): Flow<List<Chat>> {
		return chatRepository.getChatsFlow(
			initFlag = initFlag,
			channelId = channelId
		).map { chats -> chats.map { it.attachUser() } }
			.onEach { chats ->
				val newestChatInList =
					chats.firstOrNull { chat -> chat.state == ChatState.SUCCESS }
				newestChatInList?.chatId?.let { chatId ->
					channelRepository.updateChannelLastChatIfValid(channelId, chatId)
					channelRepository.updateLastReadChatIdIfValid(channelId, chatId)
				}
			}
	}

}