package com.example.bookchat.data.network

import com.example.bookchat.BuildConfig.TOKEN_RENEWAL_URL
import com.example.bookchat.data.Token
import com.example.bookchat.data.response.ForbiddenException
import com.example.bookchat.data.response.TokenRenewalFailException
import com.example.bookchat.utils.DataStoreManager
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class AppInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val bookChatToken = DataStoreManager.getBookChatTokenSync().getOrNull()
        var response = requestWithAccessToken(chain, bookChatToken?.accessToken)

        response = renewTokenOrPass(response, chain, bookChatToken)
        if (response.isSuccessful) return response

        val networkException = createException(response.code, null)
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
    ): Token {
        val tokenRenewalResponse = requestTokenRenewal(chain, refreshToken)
        if (!tokenRenewalResponse.isSuccessful) {
            // TODO : 리프레시 토큰마저 만료되었음으로 새로 로그인 해야함
            //  모든 작업 종료하고 로그인 페이지로 이동하게 수정
            throw TokenRenewalFailException()
        }
        val token = parseResponseToToken(tokenRenewalResponse.body?.string())
        DataStoreManager.saveBookChatTokenSync(token)
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

    //요청 하는 곳 마다 전부 예외처리 되어있는지 확인해보자.
    //채팅 connect요청도 이거임(예외처리해야함)
    private fun createException(
        httpCode: Int,
        errorResponseString: String?
    ): Exception? =
        when (httpCode) {
            400 -> null //BadRequestException(errorResponseString)
            403 -> ForbiddenException(errorResponseString)
            else -> null
        }

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
        return this.code == 401
    }

    private fun <T> getJsonRequestBody(
        content: T,
        contentType: String
    ): RequestBody {
        val jsonString = Gson().toJson(content)
        val requestBody = RequestBody.create(contentType.toMediaTypeOrNull(), jsonString)
        return requestBody
    }

    private fun parseResponseToToken(response: String?): Token {
        return Gson().fromJson(response, Token::class.java)
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
        private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    }
}