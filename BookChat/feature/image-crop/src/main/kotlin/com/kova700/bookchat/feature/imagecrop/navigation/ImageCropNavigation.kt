package com.kova700.bookchat.feature.imagecrop.navigation

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.kova700.bookchat.core.navigation.ImageCropNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator.ImageCropPurpose
import com.kova700.bookchat.feature.imagecrop.ImageCropActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

internal class ImageCropNavigatorImpl @Inject constructor() : ImageCropNavigator {
	override fun navigate(
		currentActivity: Activity,
		intentAction: Intent.() -> Intent,
		imageCropPurpose: ImageCropPurpose,
		resultLauncher: ActivityResultLauncher<Intent>,
	) {
		resultLauncher.launch(
			Intent(currentActivity, ImageCropActivity::class.java)
				.putExtra(ImageCropActivity.EXTRA_CROP_PURPOSE, imageCropPurpose)
				.intentAction()
		)
	}
}

@Module
@InstallIn(SingletonComponent::class)
internal interface ImageCropNavigatorModule {
	@Binds
	@Singleton
	fun bindImageCropNavigator(
		imageCropNavigator: ImageCropNavigatorImpl,
	): ImageCropNavigator
}