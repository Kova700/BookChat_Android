package com.example.bookchat.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.response.CursorMeta
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.response.ResponseGetAgony
import retrofit2.Response

class AgonyPagingSource : PagingSource<Int,Agony>(){
    private lateinit var response : Response<ResponseGetAgony>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Agony> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
        val page = params.key ?: STARTING_PAGE_INDEX

        try {
            response = App.instance.bookChatApiClient.getAgony(
                size = params.loadSize.toString(),
                postCursorId = page
            )
        }catch (e :Exception){
            return LoadResult.Error(e)
        }

        when(response.code()){
            200 -> {
                val result = response.body()
                result?.let {
                    val pagedAgony = result.agonyResponseList
                    val meta = result.cursorMeta
                    return getLoadResult(pagedAgony, page, meta)
                }
                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
            }
            else -> return LoadResult.Error(Exception(createExceptionMessage(response.code(),response.errorBody()?.string())))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Agony>): Int {
        return 0
    }

    private fun getLoadResult(
        data :List<Agony>,
        nowPage :Int,
        cursorMeta : CursorMeta
    ): LoadResult<Int, Agony>{
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