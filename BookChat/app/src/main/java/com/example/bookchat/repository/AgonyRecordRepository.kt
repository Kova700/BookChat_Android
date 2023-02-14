package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.request.RequestMakeAgonyRecord
import com.example.bookchat.data.request.RequestReviseAgonyRecord
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.utils.Constants.TAG
import javax.inject.Inject

class AgonyRecordRepository @Inject constructor() {

    suspend fun makeAgonyRecord(
        bookShelfId: Long,
        agonyId: Long,
        title: String,
        content: String
    ) {
        Log.d(TAG, "AgonyRecordRepository: makeAgonyRecord() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestMakeAgonyRecord = RequestMakeAgonyRecord(title, content)
        val response = App.instance.bookChatApiClient.makeAgonyRecord(bookShelfId, agonyId, requestMakeAgonyRecord)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun reviseAgonyRecord(
        bookShelfId: Long,
        agonyId: Long,
        recordId: Long,
        newTitle :String,
        newContent :String
    ){
        Log.d(TAG, "AgonyRecordRepository: reviseAgonyRecord() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestReviseAgonyRecord = RequestReviseAgonyRecord(newTitle, newContent)
        val response = App.instance.bookChatApiClient.reviseAgonyRecord(bookShelfId, agonyId, recordId, requestReviseAgonyRecord)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteAgonyRecord(
        bookShelfId: Long,
        agonyId: Long,
        recordId: Long
    ){
        Log.d(TAG, "AgonyRecordRepository: deleteAgonyRecord() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.deleteAgonyRecord(bookShelfId, agonyId, recordId)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun isNetworkConnected(): Boolean {
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }
}