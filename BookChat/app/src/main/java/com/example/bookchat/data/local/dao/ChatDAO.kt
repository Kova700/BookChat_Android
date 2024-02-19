package com.example.bookchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bookchat.App
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.data.local.entity.ChatWithUser
import com.example.bookchat.utils.DateManager
import kotlin.math.max

@Dao
interface ChatDAO {
    @Query("SELECT * FROM Chat " +
            "WHERE chat_room_id = :chatRoomId "+
            "ORDER BY status, chat_id DESC")
    fun pagingSource(chatRoomId: Long): PagingSource<Int, ChatEntity>

    @Transaction
    @Query("SELECT * FROM Chat " +
            "WHERE chat_room_id = :chatRoomId "+
            "ORDER BY status, chat_id DESC")
    fun getChatWithUserPagingSource(chatRoomId: Long) :PagingSource<Int, ChatWithUser>

    // TODO : 채팅도 ChatRoom과 마찬가지로 이미 있으면 업데이트 ,없으면 삽입으로 수정
    //  CASCADE사용하지말 것
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChat(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(chat: ChatEntity) :Long

//    suspend fun insertOrUpdateAllChatRoom(chatRooms: List<ChatRoomEntity>){
//        for (chatRoom in chatRooms) { insertOrUpdateChatRoom(chatRoom) }
//    }

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

    @Query("SELECT MAX(chat_id) FROM Chat " +
            "WHERE chat_id BETWEEN $MIN_CHAT_ID AND 0")
    suspend fun getMaxWaitingChatId(): Long?

    suspend fun insertWaitingChat(
        roomId: Long,
        message: String
    ): Long {
        val cachedUser = App.instance.getCachedUser()

        val chat = ChatEntity(
            chatId = getWaitingChatId(),
            chatRoomId = roomId,
            senderId = cachedUser.userId,
            dispatchTime = DateManager.getCurrentDateTimeString(),
            status = ChatEntity.ChatStatus.LOADING,
            message = message,
            chatType = ChatEntity.ChatType.Mine,
        )
        return insertChat(chat)
    }

    private suspend fun getWaitingChatId(): Long {
        maxWaitingChatId =
            max(maxWaitingChatId, getMaxWaitingChatId()?.plus(1) ?: maxWaitingChatId)
        return maxWaitingChatId++
    }

    companion object {
        private const val MIN_CHAT_ID = -1_000_000_000L
        private var maxWaitingChatId = MIN_CHAT_ID
    }
}