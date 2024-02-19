package com.example.bookchat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bookchat.data.local.entity.ChatRoomStatus.AVAILABLE
import java.io.Serializable
// TODO : last_chat_id 채팅방 별 읽지 않은 채팅 수 표시할 때 사용 가능(백엔드와 협의)
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
    @ColumnInfo(name = "top_pin_num") val topPinNum: Int = 0,
    @ColumnInfo(name = "chat_room_status") val chatRoomStatus: ChatRoomStatus = AVAILABLE,
    // -----------------------테이블 분리-------------------------
    @ColumnInfo(name = "host_id") val hostId: Long? = null,
    @ColumnInfo(name = "sub_host_ids") val subHostIds: List<Long>? = null,
    @ColumnInfo(name = "guest_ids") val guestIds: List<Long>? = null,
    //Prticipaint 형식으로 가면 여기에 참여자 정보를 안가지고 있어도 되는데
    // Prticipaint형식으로 가면 아래 Book정보랑 태그정보를 어디다 두지,,
    // ChatRoomDetail테이블 따로 파서 저장하려고 했는데,,
    //그리고 채팅방 용량같은 경우는 처음부터 있어야 할 것같은 데이터인데,
    //이걸 Detail이런곳에 넣을 순 없음.. (넣을 수 있을 것같기도 하고)

    //Detail에 넣을 수 있는데,
    //유저가 채팅방을 나가거나, 들어오거나 할때,
    //Participaint식으로 관리하면 좀 더 관리하기가 쉽긴해,
    //페이징 해서 가져올 수도 있기도 하고,
    // ---------------------------------------------------------------
    @ColumnInfo(name = "book_title") val bookTitle: String? = null,
    @ColumnInfo(name = "book_authors") val bookAuthors: List<String>? = null,
    @ColumnInfo(name = "book_cover_image_url") val bookCoverImageUrl: String? = null,
    // ---------------------------------------------------------------
    @ColumnInfo(name = "room_tags") val roomTags: List<String>? = null,
    @ColumnInfo(name = "room_capacity") val roomCapacity: Int? = null
) : Serializable

enum class ChatRoomStatus {
    AVAILABLE, UNAVAILABLE
}
// TODO : -- 아래는 처음에는 안가지고 있는 데이터
//  채팅방 들어가면 채팅방 정보조회 API 호출해서 서버로부터 가져오기
//  성공 여부와 상관없이 요청이 끝나면 이전 채팅 로드,
//  만약 실패했었다면, Queue에서 해당 작업이 사라지면 안됨,
//  작업 Queue는 인터넷 재연결 Trigger를 받으면
//  남아 있는 작업 끝내기위해서 노력하기