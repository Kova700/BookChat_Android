package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.ChatRoomListItem
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetChatRoomList
import retrofit2.Response

class ChatRoomListPagingSource : PagingSource<Int, ChatRoomListItem>() {
    private lateinit var response: Response<ResponseGetChatRoomList>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatRoomListItem> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: 0

        try {
            response = App.instance.bookChatApiClient.getUserChatRoomList(
                postCursorId = page,
                size = params.loadSize.toString()
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }

        when (response.code()) {
            200 -> {
                val result = response.body()
                result?.let {
                    val pagedChatRoom = result.chatRoomList
                    val meta = result.cursorMeta
                    return getLoadResult(pagedChatRoom, meta)
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

    private fun getLoadResult(
        data: List<ChatRoomListItem>,
        cursorMeta: CursorMeta,
    ): LoadResult<Int, ChatRoomListItem> {
        return LoadResult.Page(
            data = data,
            prevKey = getPrevKey(cursorMeta),
            nextKey = getNextKey(cursorMeta)
        )
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Int? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    private fun getNextKey(cursorMeta: CursorMeta) : Int?{
        if (cursorMeta.last) return null
        return if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
    }

    override fun getRefreshKey(state: PagingState<Int, ChatRoomListItem>): Int = 0

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}