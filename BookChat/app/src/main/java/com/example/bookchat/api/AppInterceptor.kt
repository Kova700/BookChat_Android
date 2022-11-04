package com.example.bookchat.api

import android.util.Log
import com.example.bookchat.BuildConfig.TOKEN_RENEWAL_URL
import com.example.bookchat.data.Token
import com.example.bookchat.repository.UserRepository.Companion.CONTENT_TYPE_JSON
import com.example.bookchat.response.BadRequestException
import com.example.bookchat.response.ForbiddenException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.io.IOException

class AppInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain // chain : 가로채지기 직전의 요청에 대한 정보가 모두 들어있음
    ): Response{
        val bookChatToken = getBookchatToken().getOrElse { null }
        var response = chain.requestWithAccessToken(bookChatToken?.accessToken)
        Log.d(TAG, "AppInterceptor: intercept(tokenAddedRequest) - response :$response")

        if(response.tokenIsExpired()){
            val tokenRenewalResponse = requestRenewBookChatToken(chain,bookChatToken?.refreshToken)
            Log.d(TAG, "AppInterceptor: intercept(refreshTokenRequest) - : $tokenRenewalResponse")
            if (tokenRenewalResponse.isSuccessful) {
                val token = parseResponseToToken(tokenRenewalResponse.body()?.string())
                Log.d(TAG, "AppInterceptor: intercept() - token : $token")
                saveBookchatToken(token)
                val newBookChatToken = getBookchatToken().getOrElse { null }
                response = chain.requestWithAccessToken(newBookChatToken?.accessToken)
            }
        }

        if (response.isSuccessful) return response

        val networkException = createException(response.code(),null)
        networkException?.let { throw it }

        return response
    }

    private fun requestRenewBookChatToken(chain: Interceptor.Chain, refreshToken :String?) : Response{
        val refreshTokenRequest = chain.request().changeBody(refreshToken,CONTENT_TYPE_JSON)
        val response = chain.proceed(refreshTokenRequest)
        return response
    }

    private fun Interceptor.Chain.requestWithAccessToken(accessToken :String?) : Response{
        val tokenAddedRequest = this.request().addHeader(AUTHORIZATION,accessToken ?: "")
        val response = this.proceed(tokenAddedRequest)
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

    private fun getBookchatToken() = runBlocking { runCatching{ DataStoreManager.getBookchatToken() } }

    private fun saveBookchatToken(token: Token) = runBlocking { runCatching{ DataStoreManager.saveBookchatToken(token) } }

    private fun Request.addHeader(headerName :String, headerContent :String) :Request{
        return this.newBuilder()
            .addHeader(headerName, headerContent)
            .build()
    }

    private fun <T> Request.changeBody(
        content : T,
        contentType : String
    ) :Request{
        return Request.Builder()
            .url(TOKEN_RENEWAL_URL)
            .post(getJsonRequestBody(content,contentType))
            .build()
    }

    private fun Response.tokenIsExpired() :Boolean{
        return this.code() == 401
    }

    private fun <T> getJsonRequestBody(
        content : T,
        contentType : String
    ) : RequestBody {
        val jsonString = Gson().toJson(content)
        val requestBody = RequestBody.create(MediaType.parse(contentType),jsonString)
        return requestBody
    }

    private fun parseResponseToToken(response :String?) :Token{
        return Gson().fromJson(response,Token::class.java)
    }

    companion object{
        private const val AUTHORIZATION = "Authorization"
    }

}