package com.kova700.bookchat.database.chatting.internal.typeconverter

import androidx.room.TypeConverter
import com.kova700.bookchat.core.data.chat.external.model.ChatStatus

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