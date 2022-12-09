package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookchat.api.ApiClient
import com.example.bookchat.api.BookApiInterface
import com.example.bookchat.api.UserApiInterface
import com.example.bookchat.data.User
import com.example.bookchat.utils.NetworkManager
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object{
        lateinit var instance : App
            private set
    }
    val networkManager by lazy { NetworkManager() }
    val userApiInterface by lazy { ApiClient.getApiClient().create(UserApiInterface::class.java) }
    val bookApiInterface by lazy { ApiClient.getApiClient().create(BookApiInterface::class.java) }

    private var cachedUser : User? = null

    //액티비티 , 리시버 , 서비스가 생성되기 전에 어플리케이션이 시작 중일 때 실행됨
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.KAKAO_APP_KEY
        )
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