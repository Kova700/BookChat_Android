package com.example.bookchat.repository

import com.example.bookchat.App
import com.example.bookchat.data.request.RequestGetWholeChatRoomList
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.ChatSearchFilter.*
import javax.inject.Inject

class WholeChatRoomRepository @Inject constructor() {

    suspend fun getWholeChatRoomList(
        keyword: String,
        chatSearchFilter: ChatSearchFilter
    ): ResponseGetWholeChatRoomList {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestGetWholeChatRoomList = getRequestGetWholeChatRoomList(
            postCursorId = null,
            size = SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE,
            keyword = keyword,
            chatSearchFilter = chatSearchFilter
        )

        val response = App.instance.bookChatApiClient.getWholeChatRoomList(
            postCursorId = requestGetWholeChatRoomList.postCursorId,
            size = requestGetWholeChatRoomList.size,
            roomName = requestGetWholeChatRoomList.roomName,
            title = requestGetWholeChatRoomList.title,
            isbn = requestGetWholeChatRoomList.isbn,
            tags = requestGetWholeChatRoomList.tags
        )
        when (response.code()) {
            200 -> {
                val result = response.body()
                result?.let { return result }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object {
        private const val SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE = 3

        fun getRequestGetWholeChatRoomList(
            postCursorId: Long?,
            size: Int,
            keyword: String,
            chatSearchFilter: ChatSearchFilter
        ): RequestGetWholeChatRoomList {
            val requestGetWholeChatRoomList = RequestGetWholeChatRoomList(
                postCursorId = postCursorId,
                size = size
            )

            return when (chatSearchFilter) {
                ROOM_NAME -> requestGetWholeChatRoomList.copy(roomName = keyword)
                BOOK_TITLE -> requestGetWholeChatRoomList.copy(title = keyword)
                BOOK_ISBN -> requestGetWholeChatRoomList.copy(isbn = keyword)
                ROOM_TAGS -> requestGetWholeChatRoomList.copy(tags = keyword)
            }
        }
    }
}