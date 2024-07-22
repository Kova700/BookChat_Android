package com.example.bookchat.ui.imagecrop

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageCropViewModel @Inject constructor(
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<ImageCropUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<ImageCropUiState>(ImageCropUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	fun onClickFinishBtn() {
		startEvent(
			ImageCropUiEvent.CropSelectedImageArea(
				width = CROPPED_IMG_SIZE_WIDTH,
				height = CROPPED_IMG_SIZE_HEIGHT
			)
		)
	}

	fun onChangeSelectedImageUrl(uri: Uri) {
		updateState { copy(selectedImageUrl = uri) }
	}

	fun onChangeCroppedImage(byteArray: ByteArray) {
		updateState { copy(croppedImage = byteArray) }
	}

	fun onBackPressed() {
		updateState { copy(selectedImageUrl = null) }
		startEvent(ImageCropUiEvent.MoveToGallery)
	}

	fun onClickCancelBtn() {
		startEvent(ImageCropUiEvent.MoveToBack)
	}

	fun onClickMoveToGalleryBtn() {
		updateState { copy(selectedImageUrl = null) }
		startEvent(ImageCropUiEvent.MoveToGallery)
	}

	fun onClickRightRotatePictureBtn() {
		startEvent(ImageCropUiEvent.RotateImageToRight)
	}

	fun startEvent(event: ImageCropUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: ImageCropUiState.() -> ImageCropUiState) {
		_uiState.update { _uiState.value.block() }
	}

	//TODO : 유저가 자른 이미지 비율 그대로 하고
	// 픽셀 축소, 압축 하는 방향으로 구현
	// + 1대1비율 같이 비율도 정할 수 있게 기능 추가 (like 카카오톡)
	//TODO : 채팅방 이미지는 1:1 비율로하면 깨짐 (수정 가능하게 수정)
	companion object {
		private const val CROPPED_IMG_SIZE_WIDTH = 200 //임시 200
		private const val CROPPED_IMG_SIZE_HEIGHT = 200 //임시 200
	}

}