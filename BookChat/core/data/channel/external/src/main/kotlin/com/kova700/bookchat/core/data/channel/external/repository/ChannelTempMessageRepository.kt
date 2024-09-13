package com.kova700.bookchat.core.data.channel.external.repository

interface ChannelTempMessageRepository {
	suspend fun getTempMessage(channelId: Long): String?
	suspend fun saveTempMessage(channelId: Long, message: String)
	suspend fun deleteTempMessage(channelId: Long)
	suspend fun clear()
}