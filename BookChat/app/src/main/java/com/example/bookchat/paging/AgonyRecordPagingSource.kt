package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyRecord
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.data.response.ResponseGetAgonyRecord
import com.example.bookchat.utils.SearchSortOption
import retrofit2.Response

class AgonyRecordPagingSource(
    private val agony: Agony,
    private val book: BookShelfItem,
    private val sortOption: SearchSortOption
    ) : PagingSource<Int, AgonyRecord>() {
    private lateinit var response : Response<ResponseGetAgonyRecord>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AgonyRecord> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: getFirstIndex(sortOption)

        try {
            response = App.instance.bookChatApiClient.getAgonyRecord(
                bookShelfId = book.bookShelfId,
                agonyId = agony.agonyId,
                postCursorId = page,
                size = params.loadSize.toString(),
                sort = sortOption
            )
        }catch (e :Exception){
            return LoadResult.Error(e)
        }

        when(response.code()){
            200 -> {
                val result = response.body()
                result?.let {
                    val pagedAgonyRecord = result.agonyRecordResponseList
                    val meta = result.cursorMeta
                    return getLoadResult(pagedAgonyRecord, meta, sortOption)
                }
                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
            }
            else -> return LoadResult.Error(Exception(createExceptionMessage(response.code(),response.errorBody()?.string())))
        }
    }

    private fun getLoadResult(
        data :List<AgonyRecord>,
        cursorMeta: CursorMeta,
        sortOption: SearchSortOption
    ): LoadResult<Int, AgonyRecord>{
        return try {
            LoadResult.Page(
                data = data,
                prevKey = getPrevKey(cursorMeta),
                nextKey = getNextKey(cursorMeta, sortOption)
            )
        }catch (exception :Exception){
            LoadResult.Error(exception)
        }
    }

    private fun getNextKey(
        cursorMeta: CursorMeta,
        sortOption: SearchSortOption
    ) :Int?{
        if (cursorMeta.last) return null

        return when (sortOption) {
            SearchSortOption.ID_DESC,
            SearchSortOption.UPDATED_AT_DESC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId - 2 else cursorMeta.nextCursorId
            }

            SearchSortOption.ID_ASC,
            SearchSortOption.UPDATED_AT_ASC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
            }
        }
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Int? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    override fun getRefreshKey(state: PagingState<Int, AgonyRecord>): Int? {
        return getFirstIndex(sortOption)
    }

    private fun getFirstIndex(sortOption: SearchSortOption): Int? {
        return when (sortOption) {
            SearchSortOption.ID_DESC,
            SearchSortOption.UPDATED_AT_DESC -> null

            SearchSortOption.ID_ASC,
            SearchSortOption.UPDATED_AT_ASC  -> 0
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}