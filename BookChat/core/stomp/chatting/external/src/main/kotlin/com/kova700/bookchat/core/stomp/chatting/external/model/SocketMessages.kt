package com.kova700.bookchat.core.stomp.chatting.external.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

//TODO : 채팅방별 소켓이 아닌 로그인 시 소켓 연결해서 메세지 관리하려면
// NotificationMessage에도 ChatRoomId(ChannelId) 추가되어야함
@Serializable
data class NotificationMessage(
	@SerialName("chatId")
	val chatId: Long,
	@SerialName("targetId")
	val targetUserId: Long,
	@SerialName("message")
	val message: String,
	@SerialName("dispatchTime")
	val dispatchTime: String,
	@SerialName("notificationMessageType")
	val notificationMessageType: NotificationMessageType,
) : SocketMessage
// 응답 :
//{"targetId":null,
// "chatId":null,
// "message":"방장이 오픈채팅방을 종료했습니다.\n더 이상 대화를 할 수 없으며, \n채팅방을 나가면 다시 입장 할 수 없게 됩니다.",
// "dispatchTime":null,
// "notificationMessageType":"NOTICE_HOST_EXIT
// "}


//NOTICE_ENTER로 누가 들어왔는지 공지는 띄울 수 있으나
//들어온 유저의 정보가 없어서
//들어온 유저의 정보를 알수 없음으로 띄우고 있음
//NOTICE_ENTER라면 들어온 유저 정보가 필요함
//일단은 userID로 유저 정보 가져오는 API를 호출하기로 했음
//아래 4가지
//val id: Long,
//	val nickname: String,
//	val profileImageUrl: String?,
//	val defaultProfileImageType: UserDefaultProfileType

@Serializable
enum class NotificationMessageType {
	@SerialName("NOTICE_ENTER")
	NOTICE_ENTER,

	@SerialName("NOTICE_EXIT")
	NOTICE_EXIT,

	@SerialName("NOTICE_HOST_EXIT")
	NOTICE_HOST_EXIT,

	@SerialName("NOTICE_HOST_DELEGATE")
	NOTICE_HOST_DELEGATE,

	@SerialName("NOTICE_KICK")
	NOTICE_KICK,

	@SerialName("NOTICE_SUB_HOST_DISMISS")
	NOTICE_SUB_HOST_DISMISS,

	@SerialName("NOTICE_SUB_HOST_DELEGATE")
	NOTICE_SUB_HOST_DELEGATE
}