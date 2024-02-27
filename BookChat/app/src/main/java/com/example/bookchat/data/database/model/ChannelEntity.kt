package com.example.bookchat.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// TODO : last_chat_id를 이용한 읽지 않은 채팅 수는 추후 업데이트
@Entity(tableName = "Channel")
data class ChannelEntity(
	@PrimaryKey
	@ColumnInfo(name = "room_id") val roomId: Long,
	@ColumnInfo(name = "room_name") val roomName: String,
	@ColumnInfo(name = "room_socket_id") val roomSid: String,
	@ColumnInfo(name = "room_member_count") val roomMemberCount: Long,
	@ColumnInfo(name = "default_room_image_type") val defaultRoomImageType: Int,
	@ColumnInfo(name = "room_image_uri") val roomImageUri: String? = null,
	@ColumnInfo(name = "notification_flag") val notificationFlag: Boolean = true,
	@ColumnInfo(name = "top_pin_num") val topPinNum: Int = 0,
	@ColumnInfo(name = "last_chat_id") val lastChatId: Long? = null,
//	@ColumnInfo(name = "last_active_time") val lastActiveTime: String? = null,
//	@ColumnInfo(name = "last_chat_content") val lastChatContent: String? = null,
	// -----------------------infoId만 남기고 테이블 분리------------------------- 2차
	@ColumnInfo(name = "host_id") val hostId: Long? = null,
	@ColumnInfo(name = "sub_host_ids") val subHostIds: List<Long>? = null,
	@ColumnInfo(name = "guest_ids") val guestIds: List<Long>? = null,
	@ColumnInfo(name = "room_tags") val roomTags: List<String>? = null,
	@ColumnInfo(name = "room_capacity") val roomCapacity: Int? = null,
	// -----------------------bookId만 남기고 테이블 분리------------------------- 3차
	@ColumnInfo(name = "book_title") val bookTitle: String? = null,
	@ColumnInfo(name = "book_authors") val bookAuthors: List<String>? = null,
	@ColumnInfo(name = "book_cover_image_url") val bookCoverImageUrl: String? = null,
) : Serializable