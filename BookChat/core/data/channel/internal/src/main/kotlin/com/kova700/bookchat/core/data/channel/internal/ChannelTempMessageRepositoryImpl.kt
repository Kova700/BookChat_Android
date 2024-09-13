package com.kova700.bookchat.core.data.channel.internal

import com.kova700.bookchat.core.data.channel.external.repository.ChannelTempMessageRepository
import com.kova700.bookchat.core.database.chatting.external.tempmessage.TempMessageDAO
import com.kova700.bookchat.core.database.chatting.external.tempmessage.model.TempMessageEntity
import javax.inject.Inject

class ChannelTempMessageRepositoryImpl @Inject constructor(
	private val tempMessageDAO: TempMessageDAO,
) : ChannelTempMessageRepository {
	override suspend fun getTempMessage(channelId: Long): String? {
		return tempMessageDAO.getTempMessage(channelId)?.message
	}

	override suspend fun saveTempMessage(channelId: Long, message: String) {
		tempMessageDAO.insert(
			TempMessageEntity(
				chatRoomId = channelId,
				message = message
			)
		)
	}

	override suspend fun deleteTempMessage(channelId: Long) {
		tempMessageDAO.deleteChannelTempMessage(channelId)
	}

	override suspend fun clear() {
		tempMessageDAO.deleteAll()
	}
}