package com.example.bookchat.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.WholeChatRoomListItem
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetWholeChatRoomList
import com.example.bookchat.data.repository.WholeChatRoomRepository
import com.example.bookchat.utils.ChatSearchFilter
import retrofit2.Response

class SearchResultChatRoomPagingSource(
    private val keyword: String,
    private val chatSearchFilter: ChatSearchFilter
) :
    PagingSource<Long, WholeChatRoomListItem>() {

    private lateinit var response: Response<ResponseGetWholeChatRoomList>

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, WholeChatRoomListItem> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val requestSearchChatRoom = WholeChatRoomRepository.getRequestGetWholeChatRoomList(
            postCursorId = params.key,
            size = params.loadSize,
            keyword = keyword,
            chatSearchFilter = chatSearchFilter
        )

        try {
            response = App.instance.bookChatApiClient.getWholeChatRoomList(
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