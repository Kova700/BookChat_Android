package com.example.bookchat.data.repository

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChattingRepositoryFacade @Inject constructor(
	private val chatRepository: ChatRepository,
	private val channelRepository: ChannelRepository,
) : ChatRepository by chatRepository,
	ChannelRepository by channelRepository {

	override fun getChannelsFlow(): Flow<List<Channel>> {
		return channelRepository.getChannelsFlow()
	}

	override suspend fun getChannels(loadSize: Int): List<Channel> {
		//서버로 부터 가져온 채널 목록을 로컬 DB에 저장
		val channels = channelRepository.getChannels(loadSize)
		//채널의 마지막 채팅을 로컬 DB에 삽입
		chatRepository.insertAllChats(channels.mapNotNull { it.lastChat })
		return channels
	}

	override suspend fun getChatFlow(): Flow<List<Chat>> {
		return chatRepository.getChatFlow()
	}

	override suspend fun getChats(
		channelId: Long,
		size: Int
	): List<Chat> {
		//서버로부터 가져온 채팅 로컬 DB에 저장
		val chats = chatRepository.getChats(channelId, size)
		//채널의 마지막 채팅을 로컬 DB에 삽입
		chats.firstOrNull()?.chatId?.let { channelRepository.updateLastChat(channelId, it) }
		return chats
	}

}