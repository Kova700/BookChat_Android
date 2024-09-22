package com.kova700.bookchat.feature.createchannel.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.MakeChannelActivityNavigator
import com.kova700.bookchat.feature.createchannel.MakeChannelActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class MakeChannelActivityNavigatorImpl @Inject constructor() :
	MakeChannelActivityNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, MakeChannelActivity::class.java)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface MakeChannelNavigatorModule {
	@Binds
	@Singleton
	fun bindMakeChannelNavigator(
		makeChannelNavigator: MakeChannelActivityNavigatorImpl,
	): MakeChannelActivityNavigator
}
