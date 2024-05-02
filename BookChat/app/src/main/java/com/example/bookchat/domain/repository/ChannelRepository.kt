package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelDefaultImageType
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
	fun getChannelsFlow(): Flow<List<Channel>>
	fun getChannelFlow(channelId: Long): Flow<Channel>

	suspend fun getChannels(
		loadSize: Int = REMOTE_CHANNELS_LOAD_SIZE,
	): List<Channel>

	suspend fun getChannel(channelId: Long): Channel

	suspend fun enter(channel: Channel)
	suspend fun isAlreadyEntered(channelId: Long): Boolean
	suspend fun leave(channelId: Long)
	suspend fun updateMemberCount(channelId: Long, offset: Int)
	suspend fun updateLastChat(channelId: Long, chatId: Long)

	suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		defaultRoomImageType: ChannelDefaultImageType,
		channelTags: List<String>,
		selectedBook: Book,
		channelImage: ByteArray?
	): Channel

	companion object {
		const val REMOTE_CHANNELS_LOAD_SIZE = 20
	}

}