package com.example.bookchat.ui.imagecrop

import android.net.Uri

data class ImageCropUiState(
	val uiState: UiState,
	val selectedImageUrl: Uri?,
	val croppedImage: ByteArray?,
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = ImageCropUiState(
			uiState = UiState.SUCCESS,
			selectedImageUrl = null,
			croppedImage = null,
		)
	}
}

sealed class ImageCropUiEvent {
	object MoveToBack : ImageCropUiEvent()
	object MoveToGallery : ImageCropUiEvent()
	object RotateImageToRight : ImageCropUiEvent()
	data class CropSelectedImageArea(
		val width: Int,
		val height: Int,
	) : ImageCropUiEvent()

	data class FinishWithCroppedImage(
		val croppedImage: ByteArray
	) : ImageCropUiEvent()
}