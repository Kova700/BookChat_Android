package com.example.bookchat.repository

import com.example.bookchat.App
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import javax.inject.Inject

class ChatRoomManagementRepository @Inject constructor() {

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

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}