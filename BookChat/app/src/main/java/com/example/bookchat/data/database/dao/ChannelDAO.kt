package com.example.bookchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.domain.model.ChannelDefaultImageType

@Dao
interface ChannelDAO {

	@Query(
		"SELECT * FROM Channel " +
						"WHERE room_id IN (:channelIds) " +
						"ORDER BY top_pin_num DESC, last_chat_id DESC, room_id DESC"
	)
	suspend fun getChannels(channelIds: List<Long>): List<ChannelEntity>

	@Query(
		"SELECT * FROM Channel " +
						"WHERE room_id = :channelId"
	)
	suspend fun getChannel(channelId: Long): ChannelEntity?

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertIfNotPresent(channel: ChannelEntity): Long

	suspend fun upsertAllChannels(channels: List<ChannelEntity>) {
		for (channel in channels) {
			upsertChannel(channel)
		}
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
		roomMemberCount: Long,
		defaultRoomImageType: ChannelDefaultImageType,
		roomImageUri: String?
	)

	@Query(
		"UPDATE Channel SET " +
						"last_chat_id = :lastChatId " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateLastChat(
		channelId: Long,
		lastChatId: Long
	)

	@Query(
		"UPDATE Channel SET " +
						"host_id = :hostId, " +
						"room_name = :roomName, " +
						"sub_host_ids = :subHostIds, " +
						"guest_ids = :guestIds, " +
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
		hostId: Long,
		subHostIds: List<Long>?,
		guestIds: List<Long>?,
		bookTitle: String,
		bookAuthors: List<String>,
		bookCoverImageUrl: String,
		roomTags: List<String>,
		roomCapacity: Int
	)

	suspend fun isExist(channelId: Long): Boolean {
		return (getChannel(channelId) != null)
	}

	@Query(
		"UPDATE Channel SET " +
						"room_member_count = room_member_count + :offset " +
						"WHERE room_id = :channelId"
	)
	suspend fun updateMemberCount(
		channelId: Long, offset: Int
	)

	@Query("DELETE from Channel WHERE room_id = :channelId")
	suspend fun delete(channelId: Long)

	@Query("SELECT MAX(top_pin_num) FROM Channel")
	suspend fun getMaxPinNum(): Int?
}