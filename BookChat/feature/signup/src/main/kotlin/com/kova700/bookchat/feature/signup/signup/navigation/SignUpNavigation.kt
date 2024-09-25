package com.kova700.bookchat.feature.signup.signup.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.SignUpActivityNavigator
import com.kova700.bookchat.feature.signup.signup.SignUpActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class SignUpActivityNavigatorImpl @Inject constructor() : SignUpActivityNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, SignUpActivity::class.java)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface SignUpNavigatorModule {
	@Binds
	@Singleton
	fun bindSignUpNavigator(
		signUpNavigator: SignUpActivityNavigatorImpl,
	): SignUpActivityNavigator
}
