package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO : [Version 2] 채팅방별 소켓이 아닌 로그인 시 소켓 연결해서 메세지 관리하려면
// NotificationMessage에도 ChatRoomId(ChannelId) 추가되어야함

sealed interface SocketMessage

@Serializable
data class CommonMessage(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("chatRoomId")
	val channelId: Long,
	@SerialName("senderId")
	val senderId: Long,
	@SerialName("receiptId")
	val receiptId: Long,
	@SerialName("message")
	val message: String,
	@SerialName("dispatchTime")
	val dispatchTime: String,
) : SocketMessage

@Serializable
data class NotificationMessage(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("targetId")
	val targetUserId: Long? = null,
	@SerialName("message")
	val message: String,
	@SerialName("dispatchTime")
	val dispatchTime: String,
	@SerialName("notificationMessageType")
	val notificationMessageType: NotificationMessageType,
) : SocketMessage