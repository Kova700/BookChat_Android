package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import okhttp3.MultipartBody
import javax.inject.Inject

class UserChatRoomRepository @Inject constructor() {

    suspend fun makeChatRoom(
        requestMakeChatRoom: RequestMakeChatRoom,
        charRoomImage: MultipartBody.Part?
    ): UserChatRoomListItem {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.makeChatRoom(
            requestMakeChatRoom = requestMakeChatRoom,
            chatRoomImage = charRoomImage
        )

        when (response.code()) {
            201 -> {
                val roomId = response.headers()["RoomId"]?.toLong()
                    ?: throw Exception("RoomID not received")
                val roomSId = response.headers()["Location"]?.split("/")?.last()
                    ?: throw Exception("RoomSID not received")
                val roomImageUri = response.headers()["RoomImageUri"]
                return getUserChatRoomListItem(requestMakeChatRoom, roomId, roomSId, roomImageUri)
            }
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    private fun getUserChatRoomListItem(
        requestMakeChatRoom: RequestMakeChatRoom,
        roomId: Long,
        roomSId: String,
        roomImageUri: String?
    ): UserChatRoomListItem {
        return UserChatRoomListItem(
            roomId = roomId,
            roomSid = roomSId,
            roomName = requestMakeChatRoom.roomName,
            roomMemberCount = 1,
            defaultRoomImageType = requestMakeChatRoom.defaultRoomImageType,
            roomImageUri = roomImageUri
        )
    }

    suspend fun getUserChatRoomList(loadSize: Int): List<UserChatRoomListItem> {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.getUserChatRoomList(
            postCursorId = null,
            size = loadSize
        )

        when (response.code()) {
            200 -> {
                val result = response.body()
                result?.let { return result.chatRoomList }
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

}