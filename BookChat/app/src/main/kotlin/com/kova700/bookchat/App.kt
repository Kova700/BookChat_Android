package com.kova700.bookchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.kova700.bookchat.core.chatclient.ChatClient
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

	@Inject
	lateinit var chatClient: ChatClient

	override fun onCreate() {
		super.onCreate()
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		ProcessLifecycleOwner.get().lifecycle.addObserver(chatClient)
		OAuthClient.init(this)
		forcedLogoutManager.start(this)
	}

	override val workManagerConfiguration: Configuration
		get() = Configuration.Builder()
			.setWorkerFactory(workerFactory)
			.build()
}