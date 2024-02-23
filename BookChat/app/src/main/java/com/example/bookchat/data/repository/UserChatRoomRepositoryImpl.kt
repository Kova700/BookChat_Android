package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.request.RequestMakeChatRoom
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetUserChatRoomList
import com.example.bookchat.domain.repository.UserChatRoomRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class UserChatRoomRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi
) : UserChatRoomRepository {

	override suspend fun makeChatRoom(
		requestMakeChatRoom: RequestMakeChatRoom,
		charRoomImage: MultipartBody.Part?
	): UserChatRoomListItem {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val response = bookChatApi.makeChatRoom(
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

	override suspend fun getUserChatRoomList(
		loadSize: Int,
		postCursorId: Long?,
	): ResponseGetUserChatRoomList {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		return bookChatApi.getUserChatRoomList(
			postCursorId = postCursorId,
			size = loadSize
		)
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

}