package com.example.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.bookchat.BuildConfig.KAKAO_APP_KEY
import com.example.bookchat.fcm.forcedlogout.ForcedLogoutManager
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

	@Inject
	lateinit var workerFactory: HiltWorkerFactory

	@Inject
	lateinit var forcedLogoutManager: ForcedLogoutManager

	override fun onCreate() {
		super.onCreate()
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		initKakaoSdk()
		forcedLogoutManager.start(this)
	}

	private fun initKakaoSdk() {
		KakaoSdk.init(
			context = this,
			appKey = KAKAO_APP_KEY
		)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
}