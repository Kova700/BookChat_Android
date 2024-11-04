package com.kova700.core.domain.usecase.chat

import android.util.Log
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

class GetChannelChatsFlowUseCase @Inject constructor(
	private val channelRepository: ChannelRepository,
	private val chatRepository: ChatRepository,
	private val userRepository: UserRepository,
	private val clientRepository: ClientRepository,
) : BaseGetChatUseCase(userRepository, clientRepository) {
	operator fun invoke(
		initFlag: Boolean,
		channelId: Long,
	): Flow<List<Chat>> {
		return chatRepository.getChannelChatsFlow(
			initFlag = initFlag,
			channelId = channelId
		).map { chats -> chats.map { it.attachUser() } }
			.onEach { chats ->
				Log.d("ㄺ", "GetChatsFlowUseCase: invoke(channelId : $channelId) - chats :$chats")
				val newestChatInList =
					chats.firstOrNull { chat -> chat.state == ChatState.SUCCESS }
				newestChatInList?.let { chat ->
					channelRepository.updateChannelLastChatIfValid(channelId, chat, "GetChatsFlowUseCase")
					channelRepository.updateLastReadChatIdIfValid(channelId, chat)
				}
			}
	}

}