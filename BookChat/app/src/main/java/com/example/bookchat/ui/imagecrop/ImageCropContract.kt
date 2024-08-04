package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.net.Uri
import com.example.bookchat.ui.imagecrop.model.ImageCropAspectRatio
import com.example.bookchat.ui.imagecrop.model.ImageCropPurpose

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
	object MoveToGallery : ImageCropUiEvent()
	object RotateImageToRight : ImageCropUiEvent()
	object CaptureWholeImage : ImageCropUiEvent()
	data class CropSelectedImageArea(
		val width: Int,
		val height: Int,
	) : ImageCropUiEvent()

	data class FinishWithCroppedImage(
		val croppedImageBitmap: Bitmap,
	) : ImageCropUiEvent()
}