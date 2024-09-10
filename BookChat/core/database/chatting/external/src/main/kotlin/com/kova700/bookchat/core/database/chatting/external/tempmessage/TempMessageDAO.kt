package com.kova700.bookchat.core.database.chatting.external.tempmessage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kova700.bookchat.core.database.chatting.external.tempmessage.model.TEMP_MESSAGE_TABLE_NAME
import com.kova700.bookchat.core.database.chatting.external.tempmessage.model.TempMessageEntity

@Dao
interface TempMessageDAO {

	@Query(
		"SELECT * FROM TempMessage " +
						"WHERE chat_room_id = :roomId"
	)
	suspend fun getTempMessage(roomId: Long): TempMessageEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(tempMessageEntity: TempMessageEntity): Long

	@Query(
		"DELETE FROM TempMessage " +
						"WHERE chat_room_id = :channelId"
	)
	suspend fun deleteChannelTempMessage(channelId: Long)

	@Query("DELETE FROM $TEMP_MESSAGE_TABLE_NAME")
	suspend fun deleteAll()
}