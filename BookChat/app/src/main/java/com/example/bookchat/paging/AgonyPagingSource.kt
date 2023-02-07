package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.response.CursorMeta
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.response.ResponseGetAgony
import com.example.bookchat.utils.SearchSortOption
import retrofit2.Response

class AgonyPagingSource(
    private val book: BookShelfItem,
    private val sortOption: SearchSortOption
) : PagingSource<Int, Agony>() {
    private lateinit var response: Response<ResponseGetAgony>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Agony> {
        if (!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: getFirstIndex(sortOption)

        try {
            response = App.instance.bookChatApiClient.getAgony(
                bookShelfId = book.bookShelfId,
                size = params.loadSize.toString(),
                sort = sortOption,
                postCursorId = page
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }

        when (response.code()) {
            200 -> {
                val result = response.body()
                result?.let {
                    val pagedAgony = result.agonyResponseList
                    val meta = result.cursorMeta
                    return getLoadResult(pagedAgony, meta, sortOption)
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

    override fun getRefreshKey(state: PagingState<Int, Agony>): Int? {
        return if (sortOption == SearchSortOption.DESC) null else 0
    }

    private fun getLoadResult(
        data: List<Agony>,
        cursorMeta: CursorMeta,
        sortOption: SearchSortOption
    ): LoadResult<Int, Agony> {
        return try {
            LoadResult.Page(
                data = data,
                prevKey = getPrevKey(cursorMeta),
                nextKey = getNextKey(cursorMeta, sortOption)
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Int? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    private fun getNextKey(
        cursorMeta: CursorMeta,
        sortOption: SearchSortOption
    ): Int? {
        if (cursorMeta.last) return null

        return when (sortOption) {
            SearchSortOption.DESC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId - 2 else cursorMeta.nextCursorId
            }
            SearchSortOption.ASC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
            }
        }
    }

    private fun getFirstIndex(sortOption: SearchSortOption): Int? {
        return when (sortOption) {
            SearchSortOption.DESC -> null
            SearchSortOption.ASC -> 0
        }
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode: Int, responseErrorBody: String?): String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}