package com.example.bookchat.data.paging

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
import com.example.bookchat.utils.SearchSortOption.ID_ASC
import com.example.bookchat.utils.SearchSortOption.ID_DESC
import com.example.bookchat.utils.SearchSortOption.UPDATED_AT_ASC
import com.example.bookchat.utils.SearchSortOption.UPDATED_AT_DESC
import retrofit2.Response

class AgonyRecordPagingSource(
    private val agony: Agony,
    private val book: BookShelfItem,
    private val sortOption: SearchSortOption
    ) : PagingSource<Long, AgonyRecord>() {
    private lateinit var response : Response<ResponseGetAgonyRecord>

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, AgonyRecord> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())

        val page = params.key ?: getFirstIndex(sortOption)

        try {
            response = App.instance.bookChatApiClient.getAgonyRecord(
                bookShelfId = book.bookShelfId,
                agonyId = agony.agonyId,
                postCursorId = page,
                size = params.loadSize,
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
    ): LoadResult<Long, AgonyRecord>{
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
    ) :Long?{
        if (cursorMeta.last) return null

        return when (sortOption) {
            ID_DESC,
            UPDATED_AT_DESC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId - 2 else cursorMeta.nextCursorId
            }

            ID_ASC,
            UPDATED_AT_ASC -> {
                if (cursorMeta.first) cursorMeta.nextCursorId + 2 else cursorMeta.nextCursorId
            }
        }
    }

    private fun getPrevKey(cursorMeta: CursorMeta): Long? =
        if (cursorMeta.first) null else cursorMeta.nextCursorId

    override fun getRefreshKey(state: PagingState<Long, AgonyRecord>): Long? {
        return getFirstIndex(sortOption)
    }

    private fun getFirstIndex(sortOption: SearchSortOption): Long? {
        return when (sortOption) {
            ID_DESC,
            UPDATED_AT_DESC -> null

            ID_ASC,
            UPDATED_AT_ASC  -> 0
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}