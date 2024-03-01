package com.example.bookchat.data.response

import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChannelResponse(
	@SerializedName("roomId")
	val roomId: Long,
	@SerializedName("roomName")
	val roomName: String,
	@SerializedName("roomSid")
	val roomSid: String,
	@SerializedName("roomMemberCount")
	val roomMemberCount: Long,
	@SerializedName("defaultRoomImageType")
	val defaultRoomImageType: Int,
	//추후 Chat타입으로 Sender 유저 정보,dispatchTime과 함께 한 번에 받을 수 있으면 수정 부탁
	//TODO : 마지막 활성화 시간이 초기화 되거나 마지막 Chat을 받거나 되어야함
	@SerializedName("lastChatId")
	val lastChatId: Long,
	@SerializedName("lastChatContent")
	val lastChatContent: String,
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
) : Serializable

fun ChannelResponse.getLastChat(): Chat {
	return Chat(
		chatId = lastChatId,
		chatRoomId = roomId,
		message = lastChatContent,
		chatType = ChatType.UNKNOWN,
		dispatchTime = "", //개선 필요
		sender = null
	)
}