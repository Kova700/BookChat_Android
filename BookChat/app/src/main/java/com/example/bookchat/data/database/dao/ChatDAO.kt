package com.example.bookchat.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.bookchat.data.database.model.ChatEntity
import com.example.bookchat.data.database.model.combined.ChatWithUser
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.utils.DateManager
import kotlin.math.max

@Dao
interface ChatDAO {
	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :chatRoomId " +
						"ORDER BY status, chat_id DESC"
	)
	fun pagingSource(chatRoomId: Long): PagingSource<Int, ChatEntity>

	@Transaction
	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :chatRoomId " +
						"ORDER BY status, chat_id DESC"
	)
	fun getChatWithUserPagingSource(chatRoomId: Long): PagingSource<Int, ChatWithUser>

	@Query(
		"SELECT * FROM Chat " +
						"WHERE chat_room_id = :chatRoomId " +
						"ORDER BY status, chat_id DESC"
	)
	suspend fun getLastChatOfOtherUser(chatRoomId: Long): ChatWithUser

	// TODO : 채팅도 ChatRoom과 마찬가지로 이미 있으면 업데이트 ,없으면 삽입으로 수정
	//  CASCADE사용하지말 것
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAllChat(chats: List<ChatEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChat(chat: ChatEntity): Long

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertIgnore(chat: ChatEntity): Long

//    suspend fun insertOrUpdateAllChatRoom(chatRooms: List<ChatRoomEntity>){
//        for (chatRoom in chatRooms) { insertOrUpdateChatRoom(chatRoom) }
//    }

	@Query(
		"UPDATE Chat SET " +
						"chat_id = :newChatId, " +
						"dispatch_time = :dispatchTime, " +
						"status = :status " +
						"WHERE chat_id = :targetChatId"
	)
	suspend fun updateWaitingChat(
		newChatId: Long,
		dispatchTime: String,
		status: Int,
		targetChatId: Long,
	)

	@Query(
		"SELECT MAX(chat_id) FROM Chat " +
						"WHERE chat_id BETWEEN $MIN_CHAT_ID AND 0"
	)
	suspend fun getMaxWaitingChatId(): Long?

	suspend fun insertWaitingChat(
		roomId: Long,
		message: String,
		myUserId: Long
	): Long {
		val chat = ChatEntity(
			chatId = getWaitingChatId(),
			chatRoomId = roomId,
			senderId = myUserId,
			dispatchTime = DateManager.getCurrentDateTimeString(),
			status = ChatStatus.LOADING.code,
			message = message,
			chatType = ChatType.Mine,
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