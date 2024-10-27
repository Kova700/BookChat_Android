package com.kova700.bookchat.database.chatting.internal.typeconverter

import androidx.room.TypeConverter
import com.kova700.bookchat.core.data.chat.external.model.ChatState

class ChatStatusTypeConverter {
	@TypeConverter
	fun statusToInt(chatState: ChatState): Int {
		return chatState.code
	}

	@TypeConverter
	fun intToStatus(data: Int): ChatState {
		return ChatState.getType(data)!!
	}
}