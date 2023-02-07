package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.BookReport
import com.example.bookchat.data.request.RequestRegisterBookReport
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import javax.inject.Inject

class BookReportRepository @Inject constructor() {

    suspend fun getBookReport(book : BookShelfItem) : BookReport {
        Log.d(TAG, "BookReportRepository: getBookReport() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.getBookReport(book.bookShelfId)
        Log.d(TAG, "BookReportRepository: getBookReport() - response : $response")
        Log.d(TAG, "BookReportRepository: getBookReport() - response.body() : ${response.body()}")
        when(response.code()){
            200 -> {
                val bookReportResult = response.body()
                bookReportResult?.let { return bookReportResult }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            //데이터가 없으면 404가 와야하는데 400이 넘어오는 중
            //이런경우 진짜 에러가 터진경우와 데이터가 없는경우를 구분해서 UI를 구성할 수 없음
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun registerBookReport(
        book : BookShelfItem,
        bookReport :BookReport
    ) {
        Log.d(TAG, "BookReportRepository: registerBookReport() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestRegisterBookReport = RequestRegisterBookReport(bookReport.reportTitle, bookReport.reportContent)
        val response = App.instance.bookChatApiClient.registerBookReport(book.bookShelfId, requestRegisterBookReport)

        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteBookReport(book : BookShelfItem){
        Log.d(TAG, "BookReportRepository: deleteBookReport() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.deleteBookReport(book.bookShelfId)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun reviseBookReport(
        book : BookShelfItem,
        bookReport :BookReport
    ){
        Log.d(TAG, "BookReportRepository: reviseBookReport() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestRegisterBookReport = RequestRegisterBookReport(bookReport.reportTitle, bookReport.reportContent)
        val response = App.instance.bookChatApiClient.reviseBookReport(book.bookShelfId, requestRegisterBookReport)

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