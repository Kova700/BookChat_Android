package com.example.bookchat.domain.repository

import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.utils.ChatSearchFilter

interface WholeChatRoomRepository {
	suspend fun getWholeChatRoomList(
		keyword: String,
		chatSearchFilter: ChatSearchFilter,
		size: Int = SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE,
		postCursorId: Long? = null
	): ResponseGetWholeChatRoomList

	companion object {
		private const val SIMPLE_SEARCH_CHAT_ROOMS_LOAD_SIZE = 3
	}
}