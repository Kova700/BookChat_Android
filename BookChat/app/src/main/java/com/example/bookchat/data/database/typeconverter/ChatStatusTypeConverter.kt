package com.example.bookchat.data.database.typeconverter

import androidx.room.TypeConverter
import com.example.bookchat.domain.model.ChatStatus

class ChatStatusTypeConverter {
	@TypeConverter
	fun statusToInt(chatStatus: ChatStatus): Int {
		return chatStatus.code
	}

	@TypeConverter
	fun intToStatus(data: Int): ChatStatus {
		return ChatStatus.getType(data)!!
	}
}