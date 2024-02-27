package com.example.bookchat.data

import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.domain.model.ChatType
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

//새로운 유저 입장 후, 메세지 보내면 캐싱된 유저 정보가 없음으로
//보여줄 수 있는 유저 정보가 없음. 고로, ID만 받는게 아닌 유저 정보가 필요함
//유저 Id, 닉네임, 프로필Url
//채팅방 닉네임, 프로필 Url에 관해서는 잘 모르곘다.
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
		chatType = ChatType.Other
	)
}