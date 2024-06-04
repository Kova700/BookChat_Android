package com.example.bookchat.domain.repository

import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.ChannelMemberAuthority
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
	fun getChannelsFlow(): Flow<List<Channel>>
	fun getChannelFlow(channelId: Long): Flow<Channel>

	suspend fun getChannels(
		loadSize: Int = REMOTE_CHANNELS_LOAD_SIZE,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
	)

	suspend fun getChannel(channelId: Long): Channel
	suspend fun getChannelInfo(channelId: Long)

	suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		defaultRoomImageType: ChannelDefaultImageType,
		channelTags: List<String>,
		selectedBook: Book,
		channelImage: ByteArray?,
	): Channel

	suspend fun changeChannelSetting(
		channelId: Long,
		channelTitle: String,
		channelCapacity: Int,
		channelTags: List<String>,
		channelImage: ByteArray?,
	)

	suspend fun leaveChannel(channelId: Long)
	suspend fun leaveChannelMember(
		channelId: Long,
		targetUserId: Long,
	)

	suspend fun leaveChannelHost(
		channelId: Long,
	)

	suspend fun enterChannel(channel: Channel)
	suspend fun enterChannelMember(
		channelId: Long,
		targetUserId: Long,
	)

	suspend fun banChannelMember(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean = false,
	)

	suspend fun updateChannelMemberAuthority(
		channelId: Long,
		targetUserId: Long,
		channelMemberAuthority: ChannelMemberAuthority,
		needServer: Boolean = false,
	)

	suspend fun updateChannelHost(
		channelId: Long,
		targetUserId: Long,
		needServer: Boolean = false,
	)

	suspend fun updateLastReadChatIdIfValid(
		channelId: Long,
		chatId: Long,
	)

	suspend fun updateChannelLastChatIfValid(channelId: Long, chatId: Long)
	suspend fun isChannelAlreadyEntered(channelId: Long): Boolean

	companion object {
		private const val REMOTE_CHANNELS_LOAD_SIZE = 15
		private const val DEFAULT_RETRY_MAX_ATTEMPTS = 5
	}

	suspend fun clear()
}