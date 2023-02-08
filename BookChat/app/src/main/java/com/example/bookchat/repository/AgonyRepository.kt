package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.request.RequestMakeAgony
import com.example.bookchat.data.request.RequestReviseAgony
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.utils.Constants.TAG
import javax.inject.Inject

class AgonyRepository @Inject constructor() {

    suspend fun makeAgony(
        book :BookShelfItem,
        requestMakeAgony: RequestMakeAgony
    ){
        Log.d(TAG, "AgonyRepository: makeAgony() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.makeAgony(book.bookShelfId, requestMakeAgony)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    //고민폴더 색상 변경 UI 추가되면 색상도 변경 가능하게 수정
    suspend fun reviseAgony(
        bookShelfId: Long,
        agony :Agony,
        newTitle :String
    ){
        Log.d(TAG, "AgonyRepository: modifyAgonyTitle() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestReviseAgony = RequestReviseAgony(newTitle,agony.hexColorCode)
        val response = App.instance.bookChatApiClient.reviseAgony(bookShelfId,agony.agonyId,requestReviseAgony)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteAgony(
        bookShelfId: Long,
        agonyDataList: List<AgonyDataItem>
    ){
        Log.d(TAG, "AgonyRepository: deleteAgony() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val deleteIdListString = agonyDataList.map { it.agony.agonyId }.joinToString(",")
        val response = App.instance.bookChatApiClient.deleteAgony(bookShelfId, deleteIdListString)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}