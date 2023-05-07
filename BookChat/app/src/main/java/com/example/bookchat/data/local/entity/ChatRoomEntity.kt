package com.example.bookchat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookchat.data.UserChatRoomListItem

@Entity(tableName = "ChatRoom")
data class ChatRoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "room_id") val roomId: Long,
    @ColumnInfo(name = "room_name") val roomName: String,
    @ColumnInfo(name = "room_socket_id") val roomSid: String,
    @ColumnInfo(name = "room_member_count") val roomMemberCount: Long,
    @ColumnInfo(name = "default_room_image_type") val defaultRoomImageType: Int,
    @ColumnInfo(name = "room_image_uri") val roomImageUri: String? = null,
    @ColumnInfo(name = "last_chat_id") val lastChatId: Long? = null,
    @ColumnInfo(name = "last_active_time") val lastActiveTime: String? = null,
    @ColumnInfo(name = "last_chat_content") val lastChatContent: String? = null,
    @ColumnInfo(name = "notification_flag") val notificationFlag: Boolean = true,
    @ColumnInfo(name = "top_pin_num") val topPinNum: Int = 0

) {
    // TODO : last_chat_id를 채팅방 별 읽지 않은 채팅 수 표시할 떄 사용 가능(백엔드와 협의)
    fun toUserChatRoomListItem() =
        UserChatRoomListItem(
            roomId = roomId,
            roomName = roomName,
            roomSid = roomSid,
            roomMemberCount = roomMemberCount,
            defaultRoomImageType = defaultRoomImageType,
            roomImageUri = roomImageUri,
            lastChatId = lastChatId,
            lastActiveTime = lastActiveTime,
            lastChatContent = lastChatContent
        )
}