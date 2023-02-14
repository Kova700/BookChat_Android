package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookchat.api.*
import com.example.bookchat.data.User
import com.example.bookchat.utils.NetworkManager
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    private val networkManager by lazy { NetworkManager() }
    val bookChatApiClient: BookChatApiInterface by lazy {
        RetrofitBuilder.getApiClient().create(BookChatApiInterface::class.java)
    }

    private var cachedUser: User? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.KAKAO_APP_KEY
        )
    }

    fun isNetworkConnected(): Boolean {
        return instance.networkManager.checkNetworkState()
    }

    fun cacheUser(user: User) {
        cachedUser = user
    }

    fun getCachedUser(): User? {
        return cachedUser
    }

    fun deleteCachedUser() {
        cachedUser = null
    }

    companion object {
        lateinit var instance: App
            private set
    }
}