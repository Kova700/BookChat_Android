package com.kova700.bookchat.core.data.channel.external.repository

import com.kova700.bookchat.core.data.channel.external.model.Channel
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.channel.external.model.ChannelInfo
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.data.search.book.external.model.Book
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
	fun getChannelsFlow(): Flow<List<Channel>>
	fun getChannelFlow(channelId: Long): Flow<Channel>

	suspend fun getChannels(
		loadSize: Int = REMOTE_CHANNELS_LOAD_SIZE,
	): List<Channel>

	suspend fun getMostActiveChannels(
		loadSize: Int = REMOTE_CHANNELS_LOAD_SIZE,
		maxAttempts: Int = DEFAULT_RETRY_MAX_ATTEMPTS,
		isOfflineOnly: Boolean = false,
	): List<Channel>

	suspend fun getChannel(channelId: Long): Channel
	suspend fun getChannelInfo(channelId: Long): ChannelInfo?

	suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		channelDefaultImageType: ChannelDefaultImageType,
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
	suspend fun leaveChannelHost(channelId: Long)
	suspend fun leaveChannelMember(
		channelId: Long,
		targetUserId: Long,
	)

	suspend fun muteChannel(channelId: Long)
	suspend fun unMuteChannel(channelId: Long)

	suspend fun topPinChannel(channelId: Long)
	suspend fun unPinChannel(channelId: Long)

	suspend fun enterChannel(channel: Channel)
	suspend fun enterChannelMember(
		channelId: Long,
		targetUserId: Long,
	)

	suspend fun banChannelClient(channelId: Long)
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

	suspend fun clear()

	companion object {
		private const val REMOTE_CHANNELS_LOAD_SIZE = 15
		private const val DEFAULT_RETRY_MAX_ATTEMPTS = 5
	}
}