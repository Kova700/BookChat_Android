package com.kova700.bookchat.core.database.chatting.external.channel.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority

// TODO : [Version 2] last_read_chat_id를 이용한 읽지 않은 채팅 + 읽지 않은 유저 수는 Read Event 구현 후, 추후 업데이트
@Entity(tableName = CHANNEL_ENTITY_TABLE_NAME)
data class ChannelEntity(
	@PrimaryKey
	@ColumnInfo(name = "room_id") val roomId: Long,
	@ColumnInfo(name = "room_name") val roomName: String,
	@ColumnInfo(name = "room_socket_id") val roomSid: String,
	@ColumnInfo(name = "room_member_count") val roomMemberCount: Int,
	@ColumnInfo(name = "default_room_image_type") val defaultRoomImageType: ChannelDefaultImageType,
	@ColumnInfo(name = "room_image_uri") val roomImageUri: String? = null,
	@ColumnInfo(name = "is_notification_on") val isNotificationOn: Boolean = true,
	@ColumnInfo(name = "top_pin_num") val topPinNum: Int = 0,
	@ColumnInfo(name = "is_exploded") val isExploded: Boolean = false,
	@ColumnInfo(name = "is_banned") val isBanned: Boolean = false,
	@ColumnInfo(name = "last_chat_id") val lastChatId: Long? = null,
	@ColumnInfo(name = "last_read_chat_id") val lastReadChatId: Long? = null,
	@ColumnInfo(name = "host_id") val hostId: Long? = null,
	@ColumnInfo(name = "participant_ids") val participantIds: List<Long>? = null,
	@ColumnInfo(name = "participant_authorities") val participantAuthorities: Map<Long, ChannelMemberAuthority>? = null,
	@ColumnInfo(name = "room_tags") val roomTags: List<String>? = null,
	@ColumnInfo(name = "room_capacity") val roomCapacity: Int? = null,
	// TODO : [Version 2] bookId만 남기고 테이블 분리---------------------------------------------
	@ColumnInfo(name = "book_title") val bookTitle: String? = null,
	@ColumnInfo(name = "book_authors") val bookAuthors: List<String>? = null,
	@ColumnInfo(name = "book_cover_image_url") val bookCoverImageUrl: String? = null,
)

const val CHANNEL_ENTITY_TABLE_NAME = "Channel"