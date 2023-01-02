package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.AgonyDataItem
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.request.RequestMakeAgony
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.utils.Constants.TAG
import javax.inject.Inject

class AgonyRepository @Inject constructor() {

    suspend fun makeAgony(
        book :BookShelfItem,
        requestMakeAgony: RequestMakeAgony
    ){
        Log.d(TAG, "AgonyRepository: makeAgony() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.makeAgony(book.bookId, requestMakeAgony)

        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteAgony(
        agonyDataList: List<AgonyDataItem>
    ){
        Log.d(TAG, "AgonyRepository: deleteAgony() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val deleteIdListString = agonyDataList.map { it.agony.agonyId }.joinToString(",")
        val response = App.instance.bookChatApiClient.deleteAgony(deleteIdListString)
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