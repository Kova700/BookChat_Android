package com.example.bookchat.data.stomp.external.model

import com.google.gson.annotations.SerializedName

sealed interface SocketMessage

data class CommonMessage(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("chatRoomId")
	val channelId: Long,
	@SerializedName("senderId")
	val senderId: Long,
	@SerializedName("receiptId")
	val receiptId: Long,
	@SerializedName("message")
	val message: String,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
) : SocketMessage

//TODO : 채팅방별 소켓이 아닌 로그인 시 소켓 연결해서 메세지 관리하려면
// NotificationMessage에도 ChatRoomId(ChannelId) 추가되어야함
data class NotificationMessage(
	@SerializedName("chatId")
	val chatId: Long,
	@SerializedName("targetId")
	val targetUserId: Long,
	@SerializedName("message")
	val message: String,
	@SerializedName("dispatchTime")
	val dispatchTime: String,
	@SerializedName("notificationMessageType")
	val notificationMessageType: NotificationMessageType
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

enum class NotificationMessageType {
	NOTICE_ENTER,
	NOTICE_EXIT,
	NOTICE_HOST_EXIT,
	NOTICE_HOST_DELEGATE,
	NOTICE_KICK,
	NOTICE_SUB_HOST_DISMISS,
	NOTICE_SUB_HOST_DELEGATE
}