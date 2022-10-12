package com.example.bookchat.repository

import android.util.Log
import android.widget.Toast
import com.example.bookchat.App
import com.example.bookchat.data.Token
import com.example.bookchat.data.User
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.response.*
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository{

    suspend fun signIn() {
        Log.d(TAG, "UserRepository: login() - called")
        if(!isNetworkConnected()) {
            //추후에 스낵바 혹은 유튜브처럼 구현
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val idToken = DataStoreManager.getIdToken()
        val response = App.instance.apiInterface.signIn(idToken.token,idToken.oAuth2Provider)
        Log.d(TAG, "UserRepository: signIn() - response : ${response}")
        when(response.code()){
            200 -> {
                val token = response.body()
                Log.d(TAG, "UserRepository: signIn() - token : $token")
                if(token != null){
                    token.accessToken = "Bearer ${token.accessToken}"
                    DataStoreManager.saveBookchatToken(token)
                    return
                }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            404 ->  throw NeedToSignUpException(response.errorBody()?.string())
            403 ->  throw UnauthorizedOrBlockedUserException(response.errorBody()?.string())
            else -> throw Exception(" ${response.errorBody()?.string()} Exception ")
        }
    }

    suspend fun signUp(userInfo : UserSignUpDto) {
        Log.d(TAG, "UserRepository: signUp() - called")
        if(!isNetworkConnected()) {
            //추후에 스낵바 혹은 유튜브처럼 구현
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val idToken = DataStoreManager.getIdToken()
        Log.d(TAG, "UserRepository: signUp() - idToken.token : ${idToken.token} , idToken.idTokenProvider : ${idToken.oAuth2Provider}")
        Log.d(TAG, "UserRepository: signUp() - userInfo : $userInfo")
        val response = App.instance.apiInterface.signUp(
            idToken = idToken.token,
            idTokenProvider = idToken.oAuth2Provider,
            nickname = userInfo.nickname,
            defaultProfileImageType = userInfo.defaultProfileImageType,
            userProfileImage = userInfo.userProfileImage,
//            readingTastes = userInfo.readingTastes
        )
        Log.d(TAG, "UserRepository: signUp() - response : $response ")
        when(response.code()){
            200 -> {}
            400 -> throw BadRequestException(response.errorBody()?.string())
            401 -> throw TokenExpiredException(response.errorBody()?.string())
            else -> throw Exception(" ${response.code()} Exception ")
        }
    }

    suspend fun signOut(){
        Log.d(TAG, "UserRepository: signOut() - called")
        //저장된 북챗 JWT 토큰 / ID 토큰 삭제
        DataStoreManager.deleteBookchatToken()
        DataStoreManager.deleteIdToken()
    }

    suspend fun withdraw() {
        //회원 탈퇴후 재가입 가능 기간 정책 결정해야함
        //+ 재가입할때 캐시되어있는 User 정보 어떻게 할건지 결정
        val bookchatToken = DataStoreManager.getBookchatToken()
        val response = App.instance.apiInterface.withdraw(bookchatToken.accessToken)
        Log.d(TAG, "UserRepository: withdraw() - response.code : ${response.code()}")
        when(response.code()){
            200 -> signOut()
        }
    }

    suspend fun getUserProfile() :User{
        Log.d(TAG, "UserRepository: getUserProfile() - called")
        if(!isNetworkConnected()) {
            //추후에 스낵바 혹은 유튜브처럼 구현
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
        }
        val cachedUser = App.instance.getCachedUser()
        cachedUser?.let { return cachedUser }

        val token = DataStoreManager.getBookchatToken()
        val response = App.instance.apiInterface.getUserProfile(token.accessToken)
        Log.d(TAG, "UserRepository: getUserProfile() - response : $response")
        when(response.code()){
            200 -> {
                val user = response.body()
                Log.d(TAG, "UserRepository: getUserProfile() - user :$user")
                if(user != null){
                    App.instance.cacheUser(user)
                    return user
                }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            401 -> throw TokenExpiredException(response.errorBody()?.string())
            else -> throw Exception(" ${response.code()} Exception ")
        }
    }

    suspend fun requestTokenRenewal() : Token {
        Log.d(TAG, "UserRepository: requestTokenRenewal() - called")
        if(!isNetworkConnected()) {
            //추후에 스낵바 혹은 유튜브처럼 구현
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            throw NetworkIsNotConnectedException()
        }
        val oldToken = DataStoreManager.getBookchatToken()
        val response = App.instance.apiInterface.requestTokenRenewal(oldToken.refreshToken)
        when(response.code()){
            200 -> {
                //다른 부분들이랑 합칠 수 있을거 같음
                val token = response.body()
                if(token != null){
                    token.accessToken = "Bearer ${token.accessToken}"
                    DataStoreManager.saveBookchatToken(token)
                    return token
                }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(" ${response.code()} Exception ")
        }
    }

    suspend fun requestNameDuplicateCheck(nickName : String) {
        if(!isNetworkConnected()) {
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            throw NetworkIsNotConnectedException()
        }
        val response = App.instance.apiInterface.requestNameDuplicateCheck(nickName)
        Log.d(TAG, "UserRepository: requestNameDuplicateCheck() - response.code() : ${response.code()}")
        when(response.code()){
            200 -> {
                Log.d(TAG, "UserRepository: requestNameDuplicateCheck() - 200")
            }
            else -> throw Exception("${response.errorBody()?.string()}")
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.networkManager.checkNetworkState()
    }

}

