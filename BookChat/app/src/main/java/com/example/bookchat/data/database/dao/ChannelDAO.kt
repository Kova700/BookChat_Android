package com.example.bookchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.database.model.ChannelEntity
import com.example.bookchat.data.database.model.combined.ChannelWithInfo

@Dao
interface ChannelDAO {

//	@Query(
//		"SELECT * FROM Channel " +
//						"ORDER BY top_pin_num DESC, last_chat_id DESC, room_id DESC"
//	)
//	fun pagingSource(): PagingSource<Int, ChannelWithChat>

//	@Query(
//		"SELECT * FROM Channel " +
//						"ORDER BY top_pin_num DESC, last_chat_id DESC, room_id DESC " +
//						"LIMIT :loadSize"
//	)
//	fun getActivatedChannelList(loadSize: Int): Flow<List<ChannelWithChat>>

	@Query(
		"SELECT * FROM Channel " +
						"WHERE room_id = :roomId"
	)
	suspend fun getChannel(roomId: Long): ChannelWithInfo

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertIgnore(chatRoom: ChannelEntity): Long

	suspend fun upsertAllChannels(channels: List<ChannelEntity>) {
		for (channel in channels) {
			upsertChannel(channel)
		}
	}

	suspend fun upsertChannel(chatRoom: ChannelEntity) {
		val id = insertIgnore(chatRoom)
		if (id != -1L) return

		updateForInsert(
			roomId = chatRoom.roomId,
			roomName = chatRoom.roomName,
			roomSid = chatRoom.roomSid,
			roomMemberCount = chatRoom.roomMemberCount,
			defaultRoomImageType = chatRoom.defaultRoomImageType,
			roomImageUri = chatRoom.roomImageUri
		)
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
		defaultRoomImageType: Int,
		roomImageUri: String?
	)

	suspend fun updateLastChatIfNeeded(
		roomId: Long,
		lastChatId: Long
	) {
		val existingLastId = getChannel(roomId).chatEntity?.chatId ?: return
		if (lastChatId <= existingLastId) return
		updateLastChat(roomId, lastChatId)
	}

	@Query(
		"UPDATE Channel SET " +
						"last_chat_id = :lastChatId " +
						"WHERE room_id = :roomId"
	)
	suspend fun updateLastChat(
		roomId: Long,
		lastChatId: Long
	)

	@Query(
		"UPDATE Channel SET " +
						"host_id = :hostId, " +
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
		hostId: Long,
		subHostIds: List<Long>?,
		guestIds: List<Long>?,
		bookTitle: String,
		bookAuthors: List<String>,
		bookCoverImageUrl: String,
		roomTags: List<String>,
		roomCapacity: Int
	)

	@Query(
		"UPDATE Channel SET " +
						"room_member_count = room_member_count + :offset " +
						"WHERE room_id = :roomId"
	)
	suspend fun updateMemberCount(
		roomId: Long, offset: Int
	)

	@Query("DELETE from Channel WHERE room_id = :roomId")
	suspend fun delete(roomId: Long)

	@Query("SELECT MAX(top_pin_num) FROM Channel")
	suspend fun getMaxPinNum(): Int?
}