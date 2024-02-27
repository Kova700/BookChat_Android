package com.example.bookchat.domain.repository

import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.domain.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.MultipartBody

interface ChannelRepository {
	suspend fun getChannelInfo(roomId: Long)
	suspend fun enter(channel: Channel)
	suspend fun leave(channelId: Long)
	suspend fun updateMemberCount(channelId: Long, offset: Int)
	suspend fun updateLastChat(channelId: Long, chatId: Long)

	suspend fun makeChannel(
		requestMakeChatRoom: RequestMakeChatRoom,
		charRoomImage: MultipartBody.Part?
	): Channel

	suspend fun getChannels(
		loadSize: Int = REMOTE_CHANNELS_LOAD_SIZE,
	)

	fun getChannelsFlow(): Flow<List<Channel>>
	fun getChannelFlow(channelId: Long): StateFlow<Channel?>

	companion object {
		const val REMOTE_CHANNELS_LOAD_SIZE = 7
	}
}