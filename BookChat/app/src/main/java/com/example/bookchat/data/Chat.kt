package com.example.bookchat.data

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.ChatEntity.ChatType
import com.google.gson.annotations.SerializedName

data class Chat(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("senderId")
	val senderId: Long?,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
	@SerializedName("message")
	val message: String
) {

	fun toChatEntity(
		chatRoomId: Long,
		myUserId: Long
	): ChatEntity {


		return ChatEntity(
			chatId = this.chatId,
			chatRoomId = chatRoomId,
			senderId = this.senderId,
			dispatchTime = this.dispatchTime,
			message = this.message,
			chatType = getChatType(
				senderId = senderId,
				myUserId = myUserId
			)
		)

	}
}

fun getChatType(
	senderId: Long?,
	myUserId: Long
): ChatType {
	return when (senderId) {
		myUserId -> ChatType.Mine
		null -> ChatType.Notice
		else -> ChatType.Other
	}
}
