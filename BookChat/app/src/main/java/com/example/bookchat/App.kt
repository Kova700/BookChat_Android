package com.example.bookchat

import android.app.Application
import com.example.bookchat.api.ApiClient
import com.example.bookchat.api.ApiInterface
import com.example.bookchat.data.User
import com.example.bookchat.utils.NetworkManager
import com.kakao.sdk.common.KakaoSdk

class App : Application() {

    companion object{
        lateinit var instance : App
            private set
    }
    lateinit var networkManager: NetworkManager
    lateinit var apiInterface :ApiInterface

    private var cachedUser : User? = null

    //액티비티 , 리시버 , 서비스가 생성되기 전에 어플리케이션이 시작 중일 때 실행됨
    override fun onCreate() {
        super.onCreate()
        instance = this
        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.KAKAO_APP_KEY
        )
        inject()
    }

    private fun inject() {
        networkManager = NetworkManager()
        apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
    }

    fun isNetworkConnected() :Boolean{
        return instance.networkManager.checkNetworkState()
    }

    fun cacheUser(user :User){
        cachedUser = user
    }

    fun getCachedUser() :User?{
        return cachedUser
    }

    fun deleteCachedUser() {
        cachedUser = null
    }
}