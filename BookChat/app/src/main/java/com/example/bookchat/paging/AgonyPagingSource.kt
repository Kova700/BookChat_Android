package com.example.bookchat.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.bookchat.App
import com.example.bookchat.data.*
import com.example.bookchat.response.*
import com.example.bookchat.utils.AgonyFolderHexColor
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.StarRating
import retrofit2.Response

class AgonyPagingSource : PagingSource<Int,Agony>(){
    private lateinit var response : Response<ResponseGetAgony>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Agony> {
        if(!isNetworkConnected()) return LoadResult.Error(NetworkIsNotConnectedException())
        val page = params.key ?: STARTING_PAGE_INDEX

        /*임시 더미 데이터----STRAT*/
        val testAgonyList = listOf(
            Agony(1L,"왜 계속 null safety 처리를 해 줘야 할까?",AgonyFolderHexColor.BLACK),
            Agony(2L,"스코프 함수는 왜 쓰는 걸까?",AgonyFolderHexColor.MINT),
            Agony(3L,"Flow는 왜 존재할까?",AgonyFolderHexColor.YELLOW),
            Agony(4L,"Flow는 왜 존재할까?",AgonyFolderHexColor.GREEN),
            Agony(5L,"Flow는 왜 존재할까?",AgonyFolderHexColor.ORANGE),
            Agony(6L,"Flow는 왜 존재할까?",AgonyFolderHexColor.WHITE),
            Agony(7L,"Flow는 왜 존재할까?",AgonyFolderHexColor.PURPLE)
        )
        val testMeta = CursorMeta(1,7,true,false,true,true,1)
        return getLoadResult(testAgonyList, page, testMeta)
        /*임시 더미 데이터----END*/


//        try {
//            response = App.instance.bookChatApiClient.getAgony(
//                size = params.loadSize.toString(),
//                postCursorId = page
//            )
//        }catch (e :Exception){
//            return LoadResult.Error(e)
//        }
//
//        when(response.code()){
//            200 -> {
//                val result = response.body()
//                result?.let {
//                    val pagedAgony = result.agonyResponseList
//                    Log.d(TAG, "AgonyPagingSource: load() - pagedAgony :$pagedAgony")
//                    val meta = result.cursorMeta
//                    return getLoadResult(pagedAgony, page, meta)
//                }
//                return LoadResult.Error(ResponseBodyEmptyException(response.errorBody()?.string()))
//            }
//            else -> return LoadResult.Error(Exception(createExceptionMessage(response.code(),response.errorBody()?.string())))
//        }
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
                prevKey = if (nowPage == 1) null else nowPage - 1,
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
        if(!cursorMeta.hasNext) return null

        if (nowPage == STARTING_PAGE_INDEX){
            return 4 * STARTING_PAGE_INDEX
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
        private const val STARTING_PAGE_INDEX = 1
    }
}