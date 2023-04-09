package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetUserChatRoomList
import retrofit2.Response

class ChatRoomListPagingSource : PagingSource<Int, UserChatRoomListItem>() {
    private lateinit var response: Response<ResponseGetUserChatRoomList>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserChatRoomListItem> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        try {
            response = App.instance.bookChatApiClient.getUserChatRoomList(
                postCursorId = params.key,
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
        data: List<UserChatRoomListItem>,
        cursorMeta: CursorMeta,
    ): LoadResult<Int, UserChatRoomListItem> {
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

    override fun getRefreshKey(state: PagingState<Int, UserChatRoomListItem>): Int? =
        STARTING_PAGE_INDEX

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object {
        private val STARTING_PAGE_INDEX = null
    }
}