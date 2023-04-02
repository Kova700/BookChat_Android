package com.example.bookchat.repository

import com.example.bookchat.App
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import okhttp3.MultipartBody
import javax.inject.Inject

class ChatRoomRepository @Inject constructor() {

    suspend fun makeChatRoom(
        requestMakeChatRoom :RequestMakeChatRoom,
        charRoomImage : MultipartBody.Part?
    ) {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.makeChatRoom(
            requestMakeChatRoom = requestMakeChatRoom,
            chatRoomImage = charRoomImage
        )

        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}