package com.example.bookchat.data

import com.example.bookchat.data.database.model.ChatEntity
import com.google.gson.annotations.SerializedName

data class FCMPushMessage(
	@SerializedName("pushType")
	val pushType: PushType,
	@SerializedName("body")
	val body: FCMBody,
	@SerializedName("order")
	val order: Int?,
	@SerializedName("isLast")
	val isLast: Boolean?
)

data class FCMBody(
	@SerializedName("chatRoomId")
	val chatRoomId: Long,
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("senderId")
	val senderId: Long,
	@SerializedName("receiptId")
	val receiptId: Int,
	@SerializedName("message")
	val message: String,
	@SerializedName("dispatchTime")
	val dispatchTime: String
)

fun FCMBody.toChatEntity() : ChatEntity{
	return ChatEntity(
		chatId = chatId,
		chatRoomId = chatRoomId,
		senderId = senderId,
		message = message,
		dispatchTime = dispatchTime,
		chatType = ChatEntity.ChatType.Other
	)
}