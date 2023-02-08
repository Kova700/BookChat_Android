package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyRecord
import com.example.bookchat.data.response.CursorMeta
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseGetAgonyRecord
import retrofit2.Response

class AgonyRecordPagingSource(private val agony: Agony) : PagingSource<Int, AgonyRecord>() {
    private lateinit var response : Response<ResponseGetAgonyRecord>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AgonyRecord> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
        val page = params.key ?: STARTING_PAGE_INDEX

        val testData = TestPagingDataSource.getTestAgonyRecordPagingSource()
        return getLoadResult(testData.agonyRecordResponseList, page, testData.cursorMeta)

//        try {
//            response = App.instance.bookChatApiClient.getAgonyRecord(
//                agonyId = agony.agonyId,
//                size = params.loadSize.toString(),
//                postCursorId = page
//            )
//        }catch (e :Exception){
//            return LoadResult.Error(e)
//        }

//        when(response.code()){
//            200 -> {
//                val result = response.body()
//                result?.let {
//                    val pagedAgonyRecord = result.agonyRecordResponseList
//                    Log.d(Constants.TAG, "AgonyPagingSource: load() - pagedAgony :$pagedAgonyRecord")
//                    val meta = result.cursorMeta
//                    return getLoadResult(pagedAgonyRecord, page, meta)
//                }
//                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
//            }
//            else -> return LoadResult.Error(Exception(createExceptionMessage(response.code(),response.errorBody()?.string())))
//        }
    }

    private fun getLoadResult(
        data :List<AgonyRecord>,
        nowPage :Int,
        cursorMeta : CursorMeta
    ): LoadResult<Int, AgonyRecord>{
        return try {
            LoadResult.Page(
                data = data,
                prevKey = if (nowPage == STARTING_PAGE_INDEX) null else nowPage - 1,
                nextKey = getNextKey(nowPage, cursorMeta)
            )
        }catch (exception :Exception){
            LoadResult.Error(exception)
        }
    }

    private fun getNextKey(
        nowPage :Int,
        cursorMeta : CursorMeta
    ) :Int?{
        if(cursorMeta.last) return null

        if (nowPage == STARTING_PAGE_INDEX){
            return STARTING_PAGE_INDEX + 3
        }
        return nowPage + 1
    }

    override fun getRefreshKey(state: PagingState<Int, AgonyRecord>): Int {
        return 0
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object{
        private const val STARTING_PAGE_INDEX = 0
    }
}