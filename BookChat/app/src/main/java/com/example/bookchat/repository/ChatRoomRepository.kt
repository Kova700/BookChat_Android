package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.request.RequestSearchChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetSearchChatRoomList
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.ChatSearchFilter.BOOK_ISBN
import com.example.bookchat.utils.ChatSearchFilter.ROOM_TAGS
import com.example.bookchat.utils.ChatSearchFilter.BOOK_TITLE
import com.example.bookchat.utils.ChatSearchFilter.ROOM_NAME
import com.example.bookchat.utils.Constants.TAG
import okhttp3.MultipartBody
import javax.inject.Inject

class ChatRoomRepository @Inject constructor() {

    suspend fun makeChatRoom(
        requestMakeChatRoom: RequestMakeChatRoom,
        charRoomImage: MultipartBody.Part?
    ) {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.makeChatRoom(
            requestMakeChatRoom = requestMakeChatRoom,
            chatRoomImage = charRoomImage
        )

        when (response.code()) {
            200 -> {}
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    suspend fun simpleSearchChatRooms(
        keyword: String,
        chatSearchFilter: ChatSearchFilter
    ): ResponseGetSearchChatRoomList {
        Log.d(TAG, "ChatRoomRepository: simpleSearchChatRooms() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val requestSearchChatRoom = getSimpleSearchChatRoomsRequest(keyword, chatSearchFilter)

        val response = App.instance.bookChatApiClient.searchChatRoom(
            postCursorId = null,
            size = SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE.toString(),
            roomName = requestSearchChatRoom.roomName,
            title = requestSearchChatRoom.title,
            isbn = requestSearchChatRoom.isbn,
            tags = requestSearchChatRoom.tags
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

    private fun getSimpleSearchChatRoomsRequest(
        keyword: String,
        chatSearchFilter: ChatSearchFilter
    ): RequestSearchChatRoom {
        val requestSearchChatRoom = RequestSearchChatRoom(
            postCursorId = 0,
            size = SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE.toString()
        )
        return when (chatSearchFilter) {
            ROOM_NAME -> requestSearchChatRoom.copy(roomName = keyword)
            BOOK_TITLE -> requestSearchChatRoom.copy(title = keyword)
            BOOK_ISBN -> requestSearchChatRoom.copy(isbn = keyword)
            ROOM_TAGS -> requestSearchChatRoom.copy(tags = keyword)
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
    }
}