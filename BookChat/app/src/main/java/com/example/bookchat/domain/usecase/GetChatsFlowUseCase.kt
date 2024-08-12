package com.example.bookchat.domain.usecase

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.ClientRepository
import com.example.bookchat.domain.repository.UserRepository
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
				val newestChatInList = chats.firstOrNull { chat -> chat.status == ChatStatus.SUCCESS }
				newestChatInList?.chatId?.let { chatId ->
					channelRepository.updateChannelLastChatIfValid(channelId, chatId)
					channelRepository.updateLastReadChatIdIfValid(channelId, chatId)
				}
			}
	}

}