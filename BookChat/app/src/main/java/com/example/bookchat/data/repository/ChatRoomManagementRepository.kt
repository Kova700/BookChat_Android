package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondChatRoomInfo
import com.example.bookchat.data.response.ResponseBodyEmptyException
import javax.inject.Inject

class ChatRoomManagementRepository @Inject constructor() {
    val database = App.instance.database

    suspend fun enterChatRoom(roomId: Long) {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.enterChatRoom(roomId)

        when (response.code()) {
            200 -> {}
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    suspend fun leaveChatRoom(roomId: Long) {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.leaveChatRoom(roomId)

        when (response.code()) {
            200 -> {}
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    suspend fun getChatRoomInfo(roomId: Long) {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.getChatRoomInfo(roomId)

        when (response.code()) {
            200 -> {
                val chatRoomInfo = response.body()
                chatRoomInfo?.let { it ->
                    saveUserDataInLocalDB(it)
                    saveChatRoomDataInLocalDB(roomId, it)
                    return
                }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(
                createExceptionMessage(response.code(), response.errorBody()?.string())
            )
        }
    }

    private suspend fun saveUserDataInLocalDB(chatRoomInfo: RespondChatRoomInfo) {
        val chatRoomUserList = mutableListOf(chatRoomInfo.roomHost)
        chatRoomInfo.roomSubHostList?.let { chatRoomUserList.addAll(it) }
        chatRoomInfo.roomGuestList?.let { chatRoomUserList.addAll(it) }
        database.userDAO().insertOrUpdateAllUser(chatRoomUserList.map { it.toUserEntity() })
    }

    private suspend fun saveChatRoomDataInLocalDB(roomId: Long, chatRoomInfo: RespondChatRoomInfo) {

        // TODO : 채팅방 제목이 추가되어야함 (채팅방 제목 바뀌면 알 수가 없음)
        database.chatRoomDAO().updateDetailInfo(
            roomId = roomId,
            hostId = chatRoomInfo.roomHost.id,
            subHostIds = chatRoomInfo.roomSubHostList?.map { it.id },
            guestIds = chatRoomInfo.roomGuestList?.map { it.id },
            bookTitle = chatRoomInfo.bookTitle,
            bookAuthors = chatRoomInfo.bookAuthors,
            bookCoverImageUrl = chatRoomInfo.bookCoverImageUrl,
            roomTags = chatRoomInfo.roomTags,
            roomCapacity = chatRoomInfo.roomCapacity,
        )
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}