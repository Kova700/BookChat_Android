package com.example.bookchat.api

import android.util.Log
import com.example.bookchat.BuildConfig.TOKEN_RENEWAL_URL
import com.example.bookchat.data.Token
import com.example.bookchat.data.response.BadRequestException
import com.example.bookchat.data.response.ForbiddenException
import com.example.bookchat.data.response.TokenRenewalFailException
import com.example.bookchat.repository.UserRepository.Companion.CONTENT_TYPE_JSON
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.io.IOException

class AppInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val bookChatToken = getBookchatToken().getOrElse { null }
        var response = requestWithAccessToken(chain, bookChatToken?.accessToken)
        Log.d(TAG, "AppInterceptor: intercept(tokenAddedRequest) - response :$response")

        response = renewTokenOrPass(response, chain, bookChatToken)
        if (response.isSuccessful) return response

        val networkException = createException(response.code(), null)
        networkException?.let { throw it }

        return response
    }

    private fun renewTokenOrPass(
        response: Response,
        chain: Interceptor.Chain,
        bookChatToken: Token?
    ): Response {
        if (!response.isTokenExpired()) return response

        val newToken = renewToken(chain, bookChatToken?.refreshToken)
        return requestWithAccessToken(chain, newToken.accessToken)
    }

    private fun renewToken(
        chain: Interceptor.Chain,
        refreshToken: String?
    ) :Token{
        val tokenRenewalResponse = requestTokenRenewal(chain, refreshToken)
        Log.d(TAG, "AppInterceptor: intercept(refreshTokenRequest) - : $tokenRenewalResponse")
        if (!tokenRenewalResponse.isSuccessful) throw TokenRenewalFailException()
        val token = parseResponseToToken(tokenRenewalResponse.body()?.string())
        Log.d(TAG, "AppInterceptor: intercept() - token : $token")
        saveBookchatToken(token)
        return token
    }

    private fun requestTokenRenewal(
        chain: Interceptor.Chain,
        refreshToken: String?
    ): Response {
        val refreshTokenRequest =
            makeNewRequest(getJsonRequestBody(refreshToken, CONTENT_TYPE_JSON), TOKEN_RENEWAL_URL)
        return chain.proceed(refreshTokenRequest)
    }

    private fun requestWithAccessToken(chain: Interceptor.Chain, accessToken: String?): Response {
        val tokenAddedRequest = chain.request().addHeader(AUTHORIZATION, accessToken ?: "")
        return chain.proceed(tokenAddedRequest)
    }

    private fun createException(
        httpCode: Int,
        errorResponseString: String?
    ): Exception? =
        when (httpCode) {
            400 -> BadRequestException(errorResponseString)
            403 -> ForbiddenException(errorResponseString)
            else -> null
        }

    private fun getBookchatToken() =
        runBlocking { runCatching { DataStoreManager.getBookchatToken() } }

    private fun saveBookchatToken(token: Token) =
        runBlocking { runCatching { DataStoreManager.saveBookchatToken(token) } }

    private fun Request.addHeader(headerName: String, headerContent: String): Request {
        return this.newBuilder()
            .addHeader(headerName, headerContent)
            .build()
    }

    private fun makeNewRequest(
        requestBody: RequestBody,
        url: String
    ): Request {
        return Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
    }

    private fun Response.isTokenExpired(): Boolean {
        return this.code() == 401
    }

    private fun <T> getJsonRequestBody(
        content: T,
        contentType: String
    ): RequestBody {
        val jsonString = Gson().toJson(content)
        val requestBody = RequestBody.create(MediaType.parse(contentType), jsonString)
        return requestBody
    }

    private fun parseResponseToToken(response: String?): Token {
        return Gson().fromJson(response, Token::class.java)
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
    }

}