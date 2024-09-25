package com.kova700.bookchat.feature.channel.chatting.navigation

import android.app.Activity
import android.content.Intent
import com.kova700.bookchat.core.navigation.ChannelNavigator
import com.kova700.bookchat.feature.channel.chatting.ChannelActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class ChannelNavigatorImpl @Inject constructor() : ChannelNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		shouldFinish: Boolean,
		channelId: Long,
	) {
		currentActivity.startActivity(
			Intent(currentActivity, ChannelActivity::class.java)
				.putExtra(ChannelActivity.EXTRA_CHANNEL_ID, channelId)
				.intentAction()
		)
		if (shouldFinish) currentActivity.finish()
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface ChannelNavigatorModule {
	@Binds
	@Singleton
	fun bindChannelNavigator(
		channelNavigator: ChannelNavigatorImpl,
	): ChannelNavigator
}
