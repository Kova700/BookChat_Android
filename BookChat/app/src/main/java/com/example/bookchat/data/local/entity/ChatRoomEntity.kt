package com.example.bookchat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//TODO :
// 각 채팅방 별로 들어가 있는 사람들 리스트를 가지고 있고,
// 채팅방에 들어와서 채팅방 정보 API를 통해서 채팅방에 들어와있는 사람 User정보를 받고,
// 로컬 DB에 저장해두고, 매번 채팅방에 들어올 때마다 채팅방 UserId로 해당 유저들 정보
// USER 테이블에서 가져오는 방식으로 수정
// List<User> -> HashMap<UserId, User> 이런식으로 변환 후 BindingAdapter에서 매핑해서 사용
// Key에 해당하는 Value가 없다면 "알 수 없음"같은 표시 남기기

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
    @ColumnInfo(name = "temp_saved_message") val tempSavedMessage: String? = null,
    // ---------------------------------------------------------------
    @ColumnInfo(name = "host_id") val hostId: Long? = null,
    @ColumnInfo(name = "sub_host_ids") val subHostIds: List<Long>? = null,
    @ColumnInfo(name = "guest_ids") val guestIds: List<Long>? = null,
    @ColumnInfo(name = "book_title") val bookTitle: String? = null,
    @ColumnInfo(name = "book_authors") val bookAuthors: List<String>? = null,
    @ColumnInfo(name = "book_cover_image_url") val bookCoverImageUrl: String? = null,
    @ColumnInfo(name = "room_tags") val roomTags: List<String>? = null,
    @ColumnInfo(name = "room_capacity") val roomCapacity: Int? = null
) : Serializable

// TODO : -- 아래는 처음에는 안가지고 있는 데이터
//  채팅방 들어가면 채팅방 정보조회 API 호출해서 서버로부터 가져오기
//  성공 여부와 상관없이 요청이 끝나면 이전 채팅 로드,
//  만약 실패했었다면, Queue에서 해당 작업이 사라지면 안됨,
//  작업 Queue는 인터넷 재연결 Trigger를 받으면
//  남아 있는 작업 끝내기위해서 노력하기