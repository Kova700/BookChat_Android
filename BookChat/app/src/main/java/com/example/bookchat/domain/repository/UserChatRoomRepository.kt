package com.example.bookchat.domain.repository

import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.ResponseGetUserChatRoomList
import okhttp3.MultipartBody

interface UserChatRoomRepository {
	suspend fun makeChatRoom(
		requestMakeChatRoom: RequestMakeChatRoom,
		charRoomImage: MultipartBody.Part?
	): UserChatRoomListItem

	suspend fun getUserChatRoomList(
		loadSize: Int = REMOTE_USER_CHAT_ROOM_LOAD_SIZE,
		postCursorId: Long? = null,
	): ResponseGetUserChatRoomList

	companion object {
		const val REMOTE_USER_CHAT_ROOM_LOAD_SIZE = 7
	}
}