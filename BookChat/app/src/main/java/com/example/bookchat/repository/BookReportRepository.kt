package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.BookReport
import com.example.bookchat.data.request.RequestRegisterBookReport
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.response.BookReportDoseNotExistException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import javax.inject.Inject

class BookReportRepository @Inject constructor() {

    suspend fun getBookReport(book : BookShelfItem) : BookReport {
        Log.d(TAG, "BookReportRepository: getBookReport() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.getBookReport(book.bookShelfId)
        when(response.code()){
            200 -> {
                val bookReportResult = response.body()
                bookReportResult?.let { return bookReportResult }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            404 -> throw BookReportDoseNotExistException()
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