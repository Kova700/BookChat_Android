package com.kova700.bookchat.core.database.chatting.external.channel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.channel.external.model.ChannelMemberAuthority
import com.kova700.bookchat.core.database.chatting.external.channel.model.CHANNEL_ENTITY_TABLE_NAME
import com.kova700.bookchat.core.database.chatting.external.channel.model.ChannelEntity

@Dao
interface ChannelDAO {

	@Query(
		"SELECT * FROM Channel " +
						"ORDER BY top_pin_num DESC, " +
						"last_chat_id IS NULL DESC, " +
						"last_chat_id DESC, " +
						"room_id DESC " +
						"LIMIT :pageSize OFFSET :offset"
	)
	suspend fun getMostActiveChannels(
		pageSize: Int,
		offset: Int,
	): List<ChannelEntity>

	@Query(
		"SELECT * FROM Channel " +
						"WHERE room_id = :channelId"
	)
	suspend fun getChannel(channelId: Long): ChannelEntity?

	@Query(
		"SELECT EXISTS" +
						"(SELECT * FROM Channel WHERE room_id = :channelId)"
	)
	suspend fun isChannelExist(channelId: Long): Boolean

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertIfNotPresent(channel: ChannelEntity): Long

	suspend fun upsertAllChannels(channels: List<ChannelEntity>) {
		for (channel in channels) upsertChannel(channel)
	}

	suspend fun upsertChannel(channel: ChannelEntity) {
		val id = insertIfNotPresent(channel)
		if (id != -1L) return

		updateForInsert(
			roomId = channel.roomId,
			roomName = channel.roomName,
			roomSid = channel.roomSid,
			roomMemberCount = channel.roomMemberCount,
			defaultRoomImageType = channel.defaultRoomImageType,
			roomImageUri = channel.roomImageUri
		)

		if (channel.lastChatId != null) {
			updateLastChat(
				channelId = channel.roomId,
				lastChatId = channel.lastChatId
			)
		}
	}

	@Query(
		"UPDATE Channel SET " +
						"room_name = :roomName, " +
						"room_socket_id = :roomSid, " +
						"room_member_count = :roomMemberCount, " +
						"default_room_image_type = :defaultRoomImageType, " +
						"room_image_uri = :roomImageUri " +
						"WHERE room_id = :roomId"
	)
	suspend fun updateForInsert(
		roomId: Long,
		roomName: String,
		roomSid: String,
		roomMemberCount: Int,
		defaultRoomImageType: ChannelDefaultImageType,
		roomImageUri: String?,
	)

	@Query(
		"UPDATE Channel SET " +
						"last_read_chat_id = :lastReadChatId " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateLastReadChat(
		channelId: Long,
		lastReadChatId: Long,
	)

	@Query(
		"UPDATE Channel SET " +
						"last_chat_id = :lastChatId " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateLastChat(
		channelId: Long,
		lastChatId: Long,
	)

	@Query(
		"UPDATE Channel SET " +
						"participant_ids = :participantIds, " +
						"room_member_count = :roomMemberCount, " +
						"participant_authorities = :participantAuthorities " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateChannelMember(
		channelId: Long,
		participantIds: List<Long>?,
		roomMemberCount: Int,
		participantAuthorities: Map<Long, ChannelMemberAuthority>?,
	)

	@Query(
		"UPDATE Channel SET " +
						"participant_authorities = :participantAuthorities " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateChannelMemberAuthorities(
		channelId: Long,
		participantAuthorities: Map<Long, ChannelMemberAuthority>?,
	)

	@Query(
		"UPDATE Channel SET " +
						"host_id = :targetUserId, " +
						"participant_authorities = :participantAuthorities " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateChannelHost(
		channelId: Long,
		targetUserId: Long,
		participantAuthorities: Map<Long, ChannelMemberAuthority>?,
	)

	@Query(
		"UPDATE Channel SET " +
						"room_name = :roomName, " +
						"host_id = :roomHostId, " +
						"room_member_count = :roomMemberCount, " +
						"participant_ids = :participantIds," +
						"participant_authorities = :participantAuthorities," +
						"book_title = :bookTitle, " +
						"book_authors = :bookAuthors, " +
						"book_cover_image_url = :bookCoverImageUrl, " +
						"room_tags = :roomTags, " +
						"room_capacity = :roomCapacity " +
						"WHERE room_id = :roomId"
	)
	suspend fun updateDetailInfo(
		roomId: Long,
		roomName: String,
		roomHostId: Long,
		roomMemberCount: Int,
		participantIds: List<Long>?,
		participantAuthorities: Map<Long, ChannelMemberAuthority>?,
		bookTitle: String,
		bookAuthors: List<String>,
		bookCoverImageUrl: String,
		roomTags: List<String>,
		roomCapacity: Int,
	)

	@Query("DELETE FROM Channel WHERE room_id = :channelId")
	suspend fun delete(channelId: Long)

	@Query(
		"UPDATE Channel SET " +
						"is_notification_on = :isNotificationOn " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateNotificationFlag(
		channelId: Long,
		isNotificationOn: Boolean,
	)

	@Query(
		"UPDATE Channel SET " +
						"top_pin_num = :isTopPinNum " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateTopPin(
		channelId: Long,
		isTopPinNum: Int,
	)

	@Query(
		"SELECT MAX(top_pin_num) FROM Channel"
	)
	/**존재하는 top_pin_num 중 가장 높은 번호를 반환*/
	suspend fun getMaxTopPinNum(): Int

	@Query(
		"UPDATE Channel SET " +
						"participant_ids = :participantIds, " +
						"room_member_count = :roomMemberCount, " +
						"participant_authorities = :participantAuthorities, " +
						"is_banned = :isBanned " +
						"WHERE room_id = :channelId"
	)
	suspend fun banChannelMember(
		channelId: Long,
		participantIds: List<Long>?,
		roomMemberCount: Int,
		participantAuthorities: Map<Long, ChannelMemberAuthority>?,
		isBanned: Boolean,
	)

	@Query(
		"UPDATE Channel SET " +
						"is_exploded = :isExploded " +
						"WHERE room_id = :channelId"
	)
	/** 방장이 채팅방을 나가서 채팅방이 터진 상황*/
	suspend fun updateExploded(channelId: Long, isExploded: Boolean = true)

	@Query("SELECT MAX(top_pin_num) FROM Channel")
	suspend fun getMaxPinNum(): Int?

	@Query("DELETE FROM $CHANNEL_ENTITY_TABLE_NAME")
	suspend fun deleteAll()
}