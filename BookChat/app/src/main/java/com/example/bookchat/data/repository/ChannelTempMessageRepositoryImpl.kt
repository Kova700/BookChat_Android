package com.example.bookchat.data.repository

import com.example.bookchat.data.database.dao.TempMessageDAO
import com.example.bookchat.data.database.model.TempMessageEntity
import com.example.bookchat.domain.repository.ChannelTempMessageRepository
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