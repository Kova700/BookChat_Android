package com.example.bookchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bookchat.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {
    @Query("SELECT * FROM Chat " +
            "WHERE chat_room_id = :chatRoomId "+
            "ORDER BY status, chat_id DESC")
    fun pagingSource(chatRoomId: Long): PagingSource<Int, ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChat(users: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("UPDATE Chat SET " +
            "chat_id = :chatId, " +
            "dispatch_time = :dispatchTime, " +
            "status = :status " +
            "WHERE chat_id = :targetChatId")
    suspend fun updateChatInfo(
        chatId: Long,
        dispatchTime: String,
        status: Int,
        targetChatId: Long,
    )

    @Query("SELECT MIN(chat_id) FROM Chat")
    suspend fun getMinLoadingChatId(): Long?

    @Query("SELECT MAX(chat_id) FROM Chat "+
            "WHERE chat_id BETWEEN $MIN_CHAT_ID AND 0")
    suspend fun getMaxLoadingChatId(): Long?

    @Query("SELECT * FROM Chat " +
            "WHERE chat_room_id = :chatRoomId " +
            "AND chat_id BETWEEN $MIN_CHAT_ID AND 0")
    fun getWaitingChatList(chatRoomId: Long): Flow<List<ChatEntity>>

    companion object {
        const val MIN_CHAT_ID = -1_000_000_000L
    }
}