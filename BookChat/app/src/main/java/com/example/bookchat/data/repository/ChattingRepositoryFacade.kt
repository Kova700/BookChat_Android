package com.example.bookchat.data.repository

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//TODO : 상태 clear 함수 필요
@OptIn(ExperimentalCoroutinesApi::class)
class ChattingRepositoryFacade @Inject constructor(
	private val chatRepository: ChatRepository,
	private val channelRepository: ChannelRepository,
) : ChatRepository by chatRepository,
	ChannelRepository by channelRepository {

	override fun getChatsFlow(channelId: Long): Flow<List<Chat>> {
		return getChannelFlow(channelId).flatMapLatest { channel ->
			val participants = channel.participants.associateBy { it.id }
			chatRepository.getChatsFlow(channelId).map { chats ->
				chats.map { chat -> chat.copy(sender = participants[chat.sender?.id]) }
			}
		}
	}

	override suspend fun getChats(
		channelId: Long,
		size: Int
	): List<Chat> {
		val chats = chatRepository.getChats(channelId, size)
		chats.firstOrNull()?.chatId?.let { updateLastChat(channelId, it) }
		return chats
	}

}