package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.bookchat.BuildConfig.KAKAO_APP_KEY
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		instance = this
		initKakaoSdk()
	}

	private fun initKakaoSdk() {
		KakaoSdk.init(
			context = this,
			appKey = KAKAO_APP_KEY
		)
	}

	companion object {
		lateinit var instance: App
			private set
	}
}