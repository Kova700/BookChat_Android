package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookchat.BuildConfig.KAKAO_APP_KEY
import com.example.bookchat.utils.NetworkManager
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
	private val networkManager by lazy { NetworkManager() }

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

	companion object {
		lateinit var instance: App
			private set
	}
}