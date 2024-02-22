package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.domain.repository.WholeChatRoomRepository
import com.example.bookchat.utils.ChatSearchFilter

class SearchResultChatRoomPagingSource(
	private val searchKeyword: String,
	private val chatSearchFilter: ChatSearchFilter,
	private val wholeChatRoomRepository: WholeChatRoomRepository
) : PagingSource<Long, WholeChatRoomListItem>() {

	private lateinit var response: ResponseGetWholeChatRoomList

	override suspend fun load(params: LoadParams<Long>): LoadResult<Long, WholeChatRoomListItem> {
		if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

		return try {
			val response = wholeChatRoomRepository.getWholeChatRoomList(
				postCursorId = params.key,
				size = params.loadSize,
				keyword = searchKeyword,
				chatSearchFilter = chatSearchFilter
			)
			getLoadResult(
				data = response.chatRoomList,
				cursorMeta = response.cursorMeta
			)
		} catch (e: Exception) {
			LoadResult.Error(e)
		}
	}

	override fun getRefreshKey(state: PagingState<Long, WholeChatRoomListItem>): Long? =
		STARTING_PAGE_INDEX

	private fun getLoadResult(
		data: List<WholeChatRoomListItem>,
		cursorMeta: CursorMeta,
	): LoadResult<Long, WholeChatRoomListItem> {
		return LoadResult.Page(
			data = data,
			prevKey = getPrevKey(cursorMeta),
			nextKey = getNextKey(cursorMeta)
		)
	}

	private fun getPrevKey(cursorMeta: CursorMeta): Long? =
		if (cursorMeta.first) null else cursorMeta.nextCursorId

	private fun getNextKey(cursorMeta: CursorMeta): Long? {
		if (cursorMeta.last) return null
		return if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
	}

	private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
		return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
	}

	private fun isNetworkConnected(): Boolean {
		return App.instance.isNetworkConnected()
	}

	companion object {
		private val STARTING_PAGE_INDEX = null
	}
}