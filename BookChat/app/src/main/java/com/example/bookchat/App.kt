package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookchat.BuildConfig.KAKAO_APP_KEY
import com.example.bookchat.api.BookChatApiInterface
import com.example.bookchat.api.RetrofitBuilder
import com.example.bookchat.api.StompBuilder
import com.example.bookchat.data.User
import com.example.bookchat.utils.NetworkManager
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import org.hildan.krossbow.stomp.StompClient

@HiltAndroidApp
class App : Application() {

    private val networkManager by lazy { NetworkManager() }
    private lateinit var cachedUser: User
    val bookChatApiClient: BookChatApiInterface by lazy {
        RetrofitBuilder.getApiClient().create(BookChatApiInterface::class.java)
    }
    val stompClient: StompClient by lazy { StompBuilder.getStompClient() }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        setKakaoSdk()
    }

    private fun setKakaoSdk() {
        KakaoSdk.init(
            context = this,
            appKey = KAKAO_APP_KEY
        )
    }

    fun isNetworkConnected(): Boolean {
        return instance.networkManager.checkNetworkState()
    }

    fun cacheUser(user: User) {
        cachedUser = user
    }

    fun getCachedUser(): User {
        return cachedUser
    }

    companion object {
        lateinit var instance: App
            private set
    }
}