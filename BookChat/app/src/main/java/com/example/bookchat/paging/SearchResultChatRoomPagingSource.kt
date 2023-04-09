package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.SearchChatRoomListItem
import com.example.bookchat.data.request.RequestSearchChatRoom
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetSearchChatRoomList
import com.example.bookchat.utils.ChatSearchFilter
import retrofit2.Response

class SearchResultChatRoomPagingSource(
    private val keyword: String,
    private val chatSearchFilter: ChatSearchFilter
) :
    PagingSource<Int, SearchChatRoomListItem>() {

    private lateinit var response: Response<ResponseGetSearchChatRoomList>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchChatRoomListItem> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val requestSearchChatRoom = getSimpleSearchChatRoomsRequest(
            params.key,
            params.loadSize.toString(),
            keyword,
            chatSearchFilter
        )
        try {
            response = App.instance.bookChatApiClient.searchChatRoom(
                postCursorId = requestSearchChatRoom.postCursorId,
                size = requestSearchChatRoom.size,
                roomName = requestSearchChatRoom.roomName,
                title = requestSearchChatRoom.title,
                isbn = requestSearchChatRoom.isbn,
                tags = requestSearchChatRoom.tags
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }

        when (response.code()) {
            200 -> {
                val responseGetSearchChatRoomList = response.body()
                responseGetSearchChatRoomList?.let {
                    val pagedChatRoomList = responseGetSearchChatRoomList.chatRoomList
                    val meta = responseGetSearchChatRoomList.cursorMeta
                    return getLoadResult(pagedChatRoomList, meta)
                }
                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
            }
            else -> return LoadResult.Error(
                Exception(
                    createExceptionMessage(
                        response.code(),
                        response.errorBody()?.string()
                    )
                )
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SearchChatRoomListItem>): Int? =
        STARTING_PAGE_INDEX

    private fun getLoadResult(
        data: List<SearchChatRoomListItem>,
        cursorMeta: CursorMeta,
    ): LoadResult<Int, SearchChatRoomListItem> {
        return LoadResult.Page(
            data = data,
            prevKey = getPrevKey(cursorMeta),
            nextKey = getNextKey(cursorMeta)
        )
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Int? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    private fun getNextKey(cursorMeta: CursorMeta): Int? {
        if (cursorMeta.last) return null
        return if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
    }

    private fun getSimpleSearchChatRoomsRequest(
        postCursorId: Int?,
        size: String,
        keyword: String,
        chatSearchFilter: ChatSearchFilter
    ): RequestSearchChatRoom {
        val requestSearchChatRoom = RequestSearchChatRoom(
            postCursorId = postCursorId,
            size = size
        )
        return when (chatSearchFilter) {
            ChatSearchFilter.ROOM_NAME -> requestSearchChatRoom.copy(roomName = keyword)
            ChatSearchFilter.BOOK_TITLE -> requestSearchChatRoom.copy(title = keyword)
            ChatSearchFilter.BOOK_ISBN -> requestSearchChatRoom.copy(isbn = keyword)
            ChatSearchFilter.ROOM_TAGS -> requestSearchChatRoom.copy(tags = keyword)
        }
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