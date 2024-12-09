package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO : [Version 2] ChatId를 Long타입이 아닌 String타입으로 클라이언트에서 "UUID-UserID" 형식으로
//      서버로 전송하는 구조를 사용하면 receiptId를 굳이 사용하지 않아도 되게 수정이 가능해보임
//      (하지만 쿼리 속도가 좀 느려지는 단점이 존재하긴 함)
//      현재 구조에서 메세지의 전송은 완료되었는데 소켓 끊김으로 해당 채팅을 수신하지 않으면
//      해당 채팅의 receiptId에 해당하는 전송 대기 채팅이 전송 완료 상태로 수정되지 않기 때문에
//      같은 채팅에 대한 중복 생성이 발생할 가능성이 있음

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