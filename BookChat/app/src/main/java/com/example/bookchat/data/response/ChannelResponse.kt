package com.example.bookchat.data.response

import android.util.Log
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.utils.Constants.TAG
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
	@SerializedName("roomImageUri")
	val roomImageUri: String? = null,
	//추후 Chat타입으로 Sender 유저 정보,dispatchTime과 함께 한 번에 받을 수 있으면 수정 부탁
	@SerializedName("lastChatId")
	val lastChatId: Long? = null,
	@SerializedName("lastChatContent")
	val lastChatContent: String? = null
) : Serializable

fun ChannelResponse.getLastChat(): Chat? {
	Log.d(TAG, ": getLastChat() - lastChatId :$lastChatId, lastChatContent : $lastChatContent")
	if (lastChatId == null || lastChatContent == null) return null
	return Chat(
		chatId = lastChatId,
		chatRoomId = roomId,
		message = lastChatContent,
		chatType = ChatType.UNKNOWN,
		sender = null
	)
}