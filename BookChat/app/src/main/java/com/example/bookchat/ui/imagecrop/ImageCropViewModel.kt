package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.ui.imagecrop.ImageCropActivity.Companion.EXTRA_CROP_PURPOSE
import com.example.bookchat.ui.imagecrop.model.ImageCropPurpose
import com.example.bookchat.utils.image.bitmap.scaleDownWithAspectRatio
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 이미지 가로 세로 비율 1대1 강제 해제

//TODO : 유저가 자른 이미지 비율 그대로 하고
// 픽셀 축소, 압축 하는 방향으로 구현
// + 1대1비율 같이 비율도 정할 수 있게 기능 추가 (like 카카오톡)
//TODO : 채팅방 이미지는 1:1 비율로하면 깨짐 (수정 가능하게 수정)
class ImageCropViewModel @Inject constructor(
	private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val cropPurpose = savedStateHandle.get<ImageCropPurpose>(EXTRA_CROP_PURPOSE)!!

	private val _eventFlow = MutableSharedFlow<ImageCropUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ImageCropUiState>(ImageCropUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	/** 지나친 uiState갱신 방지를 위해서 분리 */
	private val cropOverlayWidthAndHeight = MutableStateFlow<Pair<Int, Int>?>(null) //(width/height)

	init {
		initUiState()
	}

	private fun initUiState() {
		updateState { copy(cropPurpose = this@ImageCropViewModel.cropPurpose) }
	}

	private fun finishImageCrop(croppedImage: Bitmap) = viewModelScope.launch {
		val finalImage = croppedImage
			.scaleDownToFinalSize(cropPurpose)
		startEvent(ImageCropUiEvent.FinishWithCroppedImage(croppedImageBitmap = finalImage))
	}

	fun onCapturedWholeImage(wholeImage: Bitmap) {
		finishImageCrop(wholeImage)
	}

	fun onClickFinalConfirmBtn() {
		val croppedImage = _uiState.value.croppedImage
		if (croppedImage == null) {
			startEvent(ImageCropUiEvent.CaptureWholeImage)
			return
		}
		finishImageCrop(croppedImage)
	}

	fun onChangeCropOverlayWidthAndHeight(width: Int, height: Int) {
		cropOverlayWidthAndHeight.update { width to height }
	}

	fun onClickImageCropBtn(wholeImageRect: Rect) {
		updateState { copy(uiState = ImageCropUiState.UiState.IMAGE_CROP) }
		cropOverlayWidthAndHeight.update { wholeImageRect.width() to wholeImageRect.height() }
	}

	fun onClickCropFinishBtn() {
		if (uiState.value.selectedImageUrl == null) return
		val (cropOverlayWidth, cropOverlayHeight) = cropOverlayWidthAndHeight.value ?: return
		startEvent(
			ImageCropUiEvent.CropSelectedImageArea(
				width = cropOverlayWidth,
				height = cropOverlayHeight,
			)
		)
	}

	fun onChangeSelectedImageUrl(uri: Uri) {
		updateState {
			copy(
				selectedImageUrl = uri,
				croppedImage = null
			)
		}
	}

	fun onChangeCroppedImage(bitmap: Bitmap) {
		updateState {
			copy(
				uiState = ImageCropUiState.UiState.DEFAULT,
				croppedImage = bitmap
			)
		}
		cropOverlayWidthAndHeight.update { null }
	}

	fun onBackPressed() {
		updateState { copy(selectedImageUrl = null) }
		startEvent(ImageCropUiEvent.MoveToGallery)
	}

	fun onClickCropCancelBtn() {
		updateState { copy(uiState = ImageCropUiState.UiState.DEFAULT) }
	}

	fun onClickBackBtn() {
		onBackPressed()
	}

	fun onClickRightRotatePictureBtn() {
		startEvent(ImageCropUiEvent.RotateImageToRight)
	}

	private suspend fun Bitmap.scaleDownToFinalSize(purpose: ImageCropPurpose): Bitmap {
		return when (purpose) {
			ImageCropPurpose.USER_PROFILE ->
				scaleDownWithAspectRatio(
					maxWidth = USER_PROFILE_IMG_MAX_WIDTH,
					maxHeight = USER_PROFILE_IMG_MAX_HEIGHT
				)

			ImageCropPurpose.CHANNEL_PROFILE ->
				scaleDownWithAspectRatio(
					maxWidth = CHANNEL_PROFILE_IMG_MAX_WIDTH,
					maxHeight = CHANNEL_PROFILE_IMG_MAX_HEIGHT
				)
		}
	}

	fun startEvent(event: ImageCropUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: ImageCropUiState.() -> ImageCropUiState) {
		_uiState.update { _uiState.value.block() }
	}

	companion object {
		private const val USER_PROFILE_IMG_MAX_WIDTH = 256
		private const val USER_PROFILE_IMG_MAX_HEIGHT = 256
		private const val CHANNEL_PROFILE_IMG_MAX_WIDTH = 720
		private const val CHANNEL_PROFILE_IMG_MAX_HEIGHT = 1280
	}

}