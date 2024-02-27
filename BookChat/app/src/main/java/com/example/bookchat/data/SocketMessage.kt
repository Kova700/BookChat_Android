package com.example.bookchat.data

import com.google.gson.annotations.SerializedName

sealed interface SocketMessage {

	data class CommonMessage(
		@SerializedName("chatId")
		val chatId: Long,
		@SerializedName("senderId")
		val senderId: Long,
		@SerializedName("receiptId")
		val receiptId: Long,
		@SerializedName("dispatchTime")
		val dispatchTime: String,
		@SerializedName("message")
		val message: String,
		@SerializedName("messageType")
		val messageType: MessageType
	) : SocketMessage

	data class NotificationMessage(
		@SerializedName("targetId")
		val targetId: Long,
		@SerializedName("chatId")
		val chatId: Long,
		@SerializedName("message")
		val message: String,
		@SerializedName("dispatchTime")
		val dispatchTime: String,
		@SerializedName("messageType")
		val messageType: MessageType
	) : SocketMessage
}

//TODO : CHAT은 어차피 CommonMessage로 구분이 되니까 필요 없어보임
// + CommonMessage에도 MessageType 필드가 필요없어보임
// 또한 MessageType -> NoticeMessageType느낌으로 사용
// ENTER, EXIT에도 NOTICE를 앞에 붙이거나, 아예 전부 뺴거나,
// 채팅방 터지는경우(방장 나가는경우)에 대한 Notice도 필요함
// NOTICE_ENTER
// NOTICE_USER_EXIT
// NOTICE_HOST_EXIT
enum class MessageType {
	CHAT, ENTER, EXIT,
	NOTICE_HOST_DELEGATE,
	NOTICE_KICK,
	NOTICE_SUB_HOST_DISMISS,
	NOTICE_SUB_HOST_DELEGATE
}