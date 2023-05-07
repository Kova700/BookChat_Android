package com.example.bookchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookchat.data.local.entity.ChatRoomEntity

@Dao
interface ChatRoomDAO {
    @Query("SELECT * FROM ChatRoom " +
            "ORDER BY top_pin_num DESC, last_chat_id DESC")
    fun pagingSource(): PagingSource<Int, ChatRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChat(users: List<ChatRoomEntity>)

    @Query("UPDATE ChatRoom SET " +
            "last_chat_id = :lastChatId, " +
            "last_active_time = :lastActiveTime, " +
            "last_chat_content = :lastChatContent " +
            "WHERE room_id = :roomId")
    suspend fun updateLastChatInfo(
        roomId: Long,
        lastChatId: Long,
        lastActiveTime: String,
        lastChatContent: String
    )

    @Query("SELECT MAX(top_pin_num) FROM ChatRoom")
    suspend fun getMaxPinNum() :Int?
}