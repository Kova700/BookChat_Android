package com.kova700.bookchat.core.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

interface ActivityNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		shouldFinish: Boolean = false,
	)
}

interface LoginActivityNavigator : ActivityNavigator
interface SignUpActivityNavigator : ActivityNavigator
interface MakeChannelActivityNavigator : ActivityNavigator

interface ImageCropNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		imageCropPurpose: ImageCropPurpose,
		resultLauncher: ActivityResultLauncher<Intent>,
	)

	enum class ImageCropPurpose {
		USER_PROFILE, CHANNEL_PROFILE
	}

	companion object {
		const val EXTRA_CROPPED_IMAGE_CACHE_URI = "EXTRA_CROPPED_IMAGE_CACHE_URI"
	}
}

interface BookReportNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		shouldFinish: Boolean = false,
		bookshelfId: Long,
	)
}

interface AgonyNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		shouldFinish: Boolean = false,
		bookshelfId: Long,
	)
}

interface ChannelNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		shouldFinish: Boolean = false,
		channelId: Long,
	)
}

interface MainNavigator {
	fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent = { this },
		shouldFinish: Boolean = false,
	)
}