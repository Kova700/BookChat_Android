package com.example.bookchat.data.repository

import com.example.bookchat.App
import com.example.bookchat.data.api.BookChatApi
import com.example.bookchat.data.request.RequestGetWholeChatRoomList
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.domain.repository.WholeChatRoomRepository
import com.example.bookchat.utils.ChatSearchFilter
import com.example.bookchat.utils.ChatSearchFilter.BOOK_ISBN
import com.example.bookchat.utils.ChatSearchFilter.BOOK_TITLE
import com.example.bookchat.utils.ChatSearchFilter.ROOM_NAME
import com.example.bookchat.utils.ChatSearchFilter.ROOM_TAGS
import javax.inject.Inject

class WholeChatRoomRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
) : WholeChatRoomRepository {

	override suspend fun getWholeChatRoomList(
		keyword: String,
		chatSearchFilter: ChatSearchFilter,
		size: Int,
		postCursorId: Long?
	): ResponseGetWholeChatRoomList {
		if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

		val requestGetWholeChatRoomList = getRequestGetWholeChatRoomList(
			postCursorId = postCursorId,
			size = size,
			keyword = keyword,
			chatSearchFilter = chatSearchFilter
		)

		return bookChatApi.getWholeChatRoomList(
			postCursorId = requestGetWholeChatRoomList.postCursorId,
			size = requestGetWholeChatRoomList.size,
			roomName = requestGetWholeChatRoomList.roomName,
			title = requestGetWholeChatRoomList.title,
			isbn = requestGetWholeChatRoomList.isbn,
			tags = requestGetWholeChatRoomList.tags
		)
	}

	private fun getRequestGetWholeChatRoomList(
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

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

}