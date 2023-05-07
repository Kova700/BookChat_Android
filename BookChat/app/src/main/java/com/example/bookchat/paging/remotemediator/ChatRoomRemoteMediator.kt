package com.example.bookchat.paging.remotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.bookchat.api.BookChatApiInterface
import com.example.bookchat.data.local.BookChatDB
import com.example.bookchat.data.local.entity.ChatRoomEntity
import com.example.bookchat.utils.Constants.TAG

@OptIn(ExperimentalPagingApi::class)
class ChatRoomRemoteMediator(
    private val database: BookChatDB,
    private val apiClient: BookChatApiInterface
) : RemoteMediator<Int, ChatRoomEntity>() {

    private var isLast = false
    private var isFirst = true

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatRoomEntity>
    ): MediatorResult {

        val loadKey = when (loadType) {
            LoadType.REFRESH -> {
                Log.d(TAG, "ChatRoomRemoteMediator: load() - LoadType.REFRESH called")
                null
            }
            LoadType.PREPEND -> {
                Log.d(TAG, "ChatRoomRemoteMediator: load() - LoadType.PREPEND called")
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                Log.d(TAG, "ChatRoomRemoteMediator: load() - LoadType.APPEND called")
                //TODO : 끝까지 로드하지 않은 상황에서 채팅방 마지막 채팅 업데이트하면
                // 여기서 마지막 페이지 로드 요청 보내는 이슈 있음 아래 상황과 같이 해결필요
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = isLast)
                lastItem.roomId
            }
        }

        val loadSize = if (isFirst) 3 * CHAT_ROOM_LOAD_SIZE else CHAT_ROOM_LOAD_SIZE
        return try {
            val response = apiClient.getUserChatRoomList(
                postCursorId = loadKey?.toInt(),
                size = loadSize.toString()
            )

            val result = response.body()
            result?.let {
                val pagedChatRoom = result.chatRoomList.map { it.toChatRoomEntity() }
                val meta = result.cursorMeta
                isLast = meta.last
                isFirst = false
                saveChatRoomInLocalDB(pagedChatRoom)
            }

            MediatorResult.Success(endOfPaginationReached = isLast)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun saveChatRoomInLocalDB(pagedList: List<ChatRoomEntity>) {
        database.withTransaction {
            database.chatRoomDAO()
                .insertAllChat(pagedList)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return super.initialize()
    }

    //TODO : 서버와 에러 협의 필요
    companion object {
        private const val CHAT_ROOM_LOAD_SIZE = 7
    }
}