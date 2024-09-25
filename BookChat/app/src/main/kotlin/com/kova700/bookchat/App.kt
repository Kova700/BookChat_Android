package com.kova700.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutManager
import com.kova700.bookchat.core.oauth.external.OAuthClient
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
		OAuthClient.init(this)
		forcedLogoutManager.start(this)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
}