package com.example.bookchat.repository

import android.util.Log
import com.example.bookchat.App
import com.example.bookchat.data.User
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.request.RequestUserSignUp
import com.example.bookchat.response.NeedToSignUpException
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.NickNameDuplicateException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class UserRepository @Inject constructor(){

    suspend fun signIn() {
        Log.d(TAG, "UserRepository: login() - called")
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val idToken = DataStoreManager.getIdToken()
        val requestBody = getRequestBody(
            content = hashMapOf( Pair(JSON_KEY_OAUTH2PROVIDER,idToken.oAuth2Provider) ),
            contentType = CONTENT_TYPE_JSON
        )

        val response = App.instance.bookChatApiClient.signIn(idToken.token,requestBody)
        Log.d(TAG, "UserRepository: signIn() - response : ${response}")
        when(response.code()){
            200 -> {
                val token = response.body()
                token?.let { DataStoreManager.saveBookchatToken(token); return }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            404 ->  throw NeedToSignUpException(response.errorBody()?.string())
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun signUp(userSignUpDto : UserSignUpDto) {
        Log.d(TAG, "UserRepository: signUp() - called")
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val idToken = DataStoreManager.getIdToken()
        val requestUserSignUp = RequestUserSignUp(
            oauth2Provider = idToken.oAuth2Provider,
            nickname = userSignUpDto.nickname,
            defaultProfileImageType = userSignUpDto.defaultProfileImageType,
            readingTastes = userSignUpDto.readingTastes
        )

        val requestBody = getRequestBody(
            content = requestUserSignUp,
            contentType = CONTENT_TYPE_JSON
        )

        val response = App.instance.bookChatApiClient.signUp(
            idToken = idToken.token,
            userProfileImage = userSignUpDto.userProfileImage,
            userSignUpRequest = requestBody
        )
        Log.d(TAG, "UserRepository: signUp() - response : $response ")
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun signOut(){
        Log.d(TAG, "UserRepository: signOut() - called")
        DataStoreManager.deleteBookchatToken()
        DataStoreManager.deleteIdToken()
        App.instance.deleteCachedUser()
    }

    //회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
    suspend fun withdraw() {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.withdraw()
        Log.d(TAG, "UserRepository: withdraw() - response.code : ${response.code()}")
        when(response.code()){
            200 -> signOut()
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun getUserProfile() :User{
        Log.d(TAG, "UserRepository: getUserProfile() - called")
        val cachedUser = App.instance.getCachedUser()
        cachedUser?.let { return cachedUser }
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.getUserProfile()
        Log.d(TAG, "UserRepository: getUserProfile() - response : $response")
        when(response.code()){
            200 -> {
                val user = response.body()
                user?.let { App.instance.cacheUser(user); return user }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun requestNameDuplicateCheck(nickName : String) {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.requestNameDuplicateCheck(nickName)
        Log.d(TAG, "UserRepository: requestNameDuplicateCheck() - response.code() : ${response.code()}")
        when(response.code()){
            200 -> { }
            409 -> throw NickNameDuplicateException(response.errorBody()?.string())
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun <T> getRequestBody(
        content : T,
        contentType : String
    ) : RequestBody{
        val jsonString = Gson().toJson(content)
        val requestBody = RequestBody.create(MediaType.parse(contentType),jsonString)
        return requestBody
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    companion object {
        const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
        const val JSON_KEY_OAUTH2PROVIDER = "oauth2Provider"
    }

}

