package com.kova700.bookchat.feature.imagecrop

import android.graphics.Bitmap
import android.net.Uri
import com.kova700.bookchat.core.navigation.ImageCropNavigator
import com.kova700.bookchat.core.navigation.ImageCropNavigator.ImageCropPurpose
import com.kova700.bookchat.feature.imagecrop.model.ImageCropAspectRatio

data class ImageCropUiState(
	val uiState: UiState,
	val selectedImageUrl: Uri?,
	val croppedImage: Bitmap?,
	val cropPurpose: ImageCropPurpose,
	val imageCropAspectRatio: ImageCropAspectRatio,
) {
	val isDefault
		get() = uiState == UiState.DEFAULT

	val isImageCropping
		get() = uiState == UiState.IMAGE_CROP

	enum class UiState {
		DEFAULT,
		LOADING,
		IMAGE_CROP,
		ERROR,
	}

	companion object {
		val DEFAULT = ImageCropUiState(
			uiState = UiState.DEFAULT,
			selectedImageUrl = null,
			croppedImage = null,
			cropPurpose = ImageCropPurpose.USER_PROFILE,
			imageCropAspectRatio = ImageCropAspectRatio.RATIO_1_1,
		)
	}
}

sealed class ImageCropUiEvent {
	data object MoveToGallery : ImageCropUiEvent()
	data object RotateImageToRight : ImageCropUiEvent()
	data object CaptureWholeImage : ImageCropUiEvent()
	data class CropSelectedImageArea(
		val width: Int,
		val height: Int,
	) : ImageCropUiEvent()

	data class FinishWithCroppedImage(
		val croppedImageBitmap: Bitmap,
	) : ImageCropUiEvent()
}