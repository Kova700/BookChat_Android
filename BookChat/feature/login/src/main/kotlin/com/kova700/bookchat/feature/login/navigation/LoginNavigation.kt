package com.kova700.bookchat.feature.login.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.LoginActivityNavigator
import com.kova700.bookchat.feature.login.LoginActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class LoginActivityNavigatorImpl @Inject constructor() : LoginActivityNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, LoginActivity::class.java)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface LoginNavigatorModule {
	@Binds
	@Singleton
	fun bindLoginNavigator(
		loginNavigator: LoginActivityNavigatorImpl,
	): LoginActivityNavigator
}
