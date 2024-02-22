package com.example.bookchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.database.model.TempMessageEntity

@Dao
interface TempMessageDAO {

    @Query(
        "SELECT * FROM TempMessage " +
                "WHERE chat_room_id = :roomId"
    )
    suspend fun getTempMessage(roomId: Long): TempMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIgnore(tempMessageEntity: TempMessageEntity): Long

    suspend fun insertOrUpdateTempMessage(
        roomId: Long,
        message: String
    ) {
        val id = insertIgnore(
            TempMessageEntity(
                chatRoomId = roomId,
                message = message
            )
        )
        if (id != -1L) return

        setTempSavedMessage(
            roomId = roomId,
            message = message
        )
    }

    @Query(
        "UPDATE TempMessage SET " +
                "message = :message " +
                "WHERE chat_room_id = :roomId"
    )
    suspend fun setTempSavedMessage(
        roomId: Long,
        message: String
    )
}