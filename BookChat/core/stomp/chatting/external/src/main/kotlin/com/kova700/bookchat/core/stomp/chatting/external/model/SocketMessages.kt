package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO : [Version 2] FCM 안오다가 채팅방 들어갔다 나오면 FCM 받아지는 현상이 있음
//  아마 서버에서 disconnected 상태 업데이트가 아직 안되어서 FCM 수신이 안되는 듯하다
//  추후 앱 단위에서 소켓 연결하고 모든 소켓 Frame에 ChannelId, ChatId를 포함하여
//  모든 채팅방이 자동 subscribe된 채로 사용되는 형식으로 수정하해야 할듯하다.
//  connect 보내면 서버에서 subscribeAllChannels로 모든 채팅방을 subscribe되게도 가능하겠다.(어차피 모든 채팅방 구독해야하니까)

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
	@SerialName("chatRoomId")
	val channelId: Long,
	@SerialName("targetId")
	val targetUserId: Long? = null,
	@SerialName("message")
	val message: String,
	@SerialName("dispatchTime")
	val dispatchTime: String,
	@SerialName("notificationMessageType")
	val notificationMessageType: NotificationMessageType,
) : SocketMessage