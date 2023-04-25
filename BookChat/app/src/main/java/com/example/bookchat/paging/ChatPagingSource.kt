package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Chat
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.RespondGetChat
import com.example.bookchat.data.response.ResponseBodyEmptyException
import retrofit2.Response

class ChatPagingSource(
    private val roomId: Long
) : PagingSource<Int, Chat>() {
    private lateinit var response: Response<RespondGetChat>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Chat> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        try {
            response = App.instance.bookChatApiClient.getChat(
                roomId = roomId,
                size = params.loadSize.toString(),
                postCursorId = params.key
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
        when (response.code()) {
            200 -> {
                val result = response.body()
                result?.let {
                    val pagedChatList = result.chatResponseList
                    val meta = result.cursorMeta
                    return getLoadResult(pagedChatList, meta)
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
        data: List<Chat>,
        cursorMeta: CursorMeta,
    ): LoadResult<Int, Chat> {
        return try {
            LoadResult.Page(
                data = data,
                prevKey = getPrevKey(cursorMeta),
                nextKey = getNextKey(cursorMeta)
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun getNextKey(cursorMeta: CursorMeta): Int? {
        if (cursorMeta.last) return null
        return if (cursorMeta.first) cursorMeta.nextCursorId - 2 else cursorMeta.nextCursorId
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Int? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    override fun getRefreshKey(state: PagingState<Int, Chat>): Int? {
        TODO("Not yet implemented")
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

}