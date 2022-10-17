package com.example.bookchat.api

import android.util.Log
import com.example.bookchat.response.BadRequestException
import com.example.bookchat.response.ForbiddenException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AppInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain // chain : 가로채지기 직전의 요청에 대한 정보가 모두 들어있음
    ): Response{
        Log.d(TAG, "AppInterceptor: intercept() - chain.request() : ${chain.request()}")

        val getTokenResult = runBlocking {
            runCatching{ DataStoreManager.getBookchatToken() }
        } //시간이 짧음으로 runBlocking사용

        val token = getTokenResult.getOrElse { null }
        val tokenAddedRequest = chain.request().newBuilder() //앞에 요청 내용 모두 복사 (리퀘스트는 불변객체이기 때문)
            .addHeader("Authorization", token?.accessToken ?: "")
            .build() //생성
        val response = chain.proceed(tokenAddedRequest)
        Log.d(TAG, "AppInterceptor: intercept() - response(tokenAddedRequest) : $response")

        if (response.isSuccessful) return response

        val networkException = createException(response.code(),null)
        networkException?.let { throw it }

        return response
    }

    private fun createException(
        httpCode: Int,
        errorResponseString :String?
    ): Exception? =
        when(httpCode){
            400 -> BadRequestException(errorResponseString)
            403 -> ForbiddenException(errorResponseString)
            else -> null
        }
}