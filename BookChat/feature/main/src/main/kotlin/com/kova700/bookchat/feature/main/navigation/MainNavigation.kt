package com.kova700.bookchat.feature.main.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.MainNavigator
import com.kova700.bookchat.feature.main.MainActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class MainNavigatorImpl @Inject constructor() : MainNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, MainActivity::class.java).intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface MainNavigatorModule {
	@Binds
	@Singleton
	fun bindMainNavigator(
		mainNavigator: MainNavigatorImpl,
	): MainNavigator
}
