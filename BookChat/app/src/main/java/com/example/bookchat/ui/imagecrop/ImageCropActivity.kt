package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import com.example.bookchat.ui.imagecrop.model.ImageCropPurpose
import com.example.bookchat.utils.image.saveImageToCacheAndGetUri
import kotlinx.coroutines.launch

//TODO : 이미지 다 자르고 반환할떄, 돌려받은 곳 이미지가 꽉차게 안보임
//  fitCenter 혹은 fitXY해야할듯..?
//centerCrop사용해야 카톡처럼 됨 (프로필 띄우는 곳 전부 수정해야함)
// (채팅방의 경우는 동그라미(크롭 안내선) 안띄움 그냥 사각형만 있음
// + 여기도 centerCrop인거 같긴함)

//동그라미 크기를 고정시킬 수 있지 않은이상, 동그라미 쓰는 경우(유저 프로필 수정)에는 1대1을 강제해야할듯
//TODO : 채널 이미지 centerCrop으로 하고 있는데, 좌우는 centerCrop이 되고 있으나, 위 아래가 centerCrop이 안됨
//    추청 이유 1: xml에서 크기 설정에 문제가 있다.
//    추정 이유 2: xml은 잘 그려지고 있으나 이미지 좌우 너비 설정에 있어서 이미 찌그러진 채로 설정되어서 이미 centerCrop이 된 화면일 수도 잇음
//TODO : 채널 프로필 지정하고 확인 누르면 터짐 체크 필요함 (용량 초과) (파일으로 저장하고 불러오는 방식으로 권장됨)
//TODO : 비율에 맞는 이미지 자르기 UI 제공 (채널 이미지인 경우에만)
class ImageCropActivity : AppCompatActivity() {

	private lateinit var binding: ActivityImageCropBinding

	private val imageCropViewModel: ImageCropViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
		binding.lifecycleOwner = this
		moveToGallery()
		observeUiState()
		observeUiEvent()
		initViewState()
		setBackPressedDispatcher()
	}

	private fun observeUiState() = lifecycleScope.launch {
		imageCropViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		imageCropViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(uiState: ImageCropUiState) {
		setCropImageViewState(uiState)
		binding.defaultStateGroup.visibility =
			if (uiState.isDefault) View.VISIBLE else View.GONE
		binding.imageCropStateGroup.visibility =
			if (uiState.isImageCropping) View.VISIBLE else View.GONE
		binding.imageCropTitle.text =
			if (uiState.isImageCropping) getString(R.string.image_crop_title)
			else getString(R.string.select_image)
	}

	//	setAspectRatio
	// clearAspectRatio
	private fun setCropImageViewState(uiState: ImageCropUiState) {
		setCropImageViewImage(uiState)
		setCropImageViewCropOverlay(uiState)
	}

	private fun setCropImageViewImage(uiState: ImageCropUiState) {
		with(binding.cropImageView) {
			if (uiState.croppedImage != null) {
				setImageBitmap(uiState.croppedImage)
				return
			}
			if (tag != uiState.selectedImageUrl) {
				tag = uiState.selectedImageUrl
				setImageUriAsync(uiState.selectedImageUrl)
			}
		}
	}

	private fun setCropImageViewCropOverlay(uiState: ImageCropUiState) {
		with(binding.cropImageView) {
			isShowCropOverlay = uiState.isImageCropping
			cropShape = when (uiState.cropPurpose) {
				ImageCropPurpose.USER_PROFILE -> CropImageView.CropShape.OVAL
				ImageCropPurpose.CHANNEL_PROFILE -> CropImageView.CropShape.RECTANGLE
			}
		}
	}

	//TODO  : CropOverlay 모양 바꿀 수 있거나 사각형의 connerRadius 바꿀 수 있으면 스쿼클 모양 설정
	private fun initViewState() {
		with(binding) {
			backBtn.setOnClickListener { imageCropViewModel.onClickBackBtn() }
			cropCancelBtn.setOnClickListener { imageCropViewModel.onClickCropCancelBtn() }
			cropFinishBtn.setOnClickListener { imageCropViewModel.onClickCropFinishBtn() }
			finalConfirmBtn.setOnClickListener { imageCropViewModel.onClickFinalConfirmBtn() }
			imgCropBtn.setOnClickListener { imageCropViewModel.onClickImageCropBtn() }
			imgRotateBtn.setOnClickListener { imageCropViewModel.onClickRightRotatePictureBtn() }
			cropImageView.setOnSetCropOverlayMovedListener { rect ->
				rect?.let {
					imageCropViewModel.onChangeCropOverlayWidthAndHeight(
						width = rect.width(),
						height = rect.height()
					)
				}
			}
		}
	}

	private val galleryActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
			if (resultUri == null) {
				finish()
				return@registerForActivityResult
			}
			imageCropViewModel.onChangeSelectedImageUrl(resultUri)
		}

	private fun moveToGallery() {
		galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
	}

	private fun cropSelectedImageArea(width: Int, height: Int) {
		val bitmap = binding.cropImageView.getCroppedImage(
			reqWidth = width,
			reqHeight = height,
		) ?: return
		imageCropViewModel.onChangeCroppedImage(bitmap)
	}

	private fun finishWithCroppedImage(
		croppedImageBitmap: Bitmap,
	) = lifecycleScope.launch {
		intent.putExtra(
			EXTRA_CROPPED_IMAGE_CACHE_URI,
			saveImageToCacheAndGetUri(croppedImageBitmap).toString()
		)
		setResult(RESULT_OK, intent)
		finish()
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback(this) {
			if (imageCropViewModel.uiState.value.selectedImageUrl != null) {
				imageCropViewModel.onBackPressed()
				return@addCallback
			}
			finish()
		}
	}

	private fun rotateImageToRight() {
		binding.cropImageView.rotateImage(ANGLE_90)
	}

	private fun captureWholeImage() {
		with(binding.cropImageView) {
			setFixedAspectRatio(false)
			cropRect = wholeImageRect
			isShowCropOverlay = false
			val bitmap = getCroppedImage(
				reqWidth = wholeImageRect?.width() ?: return,
				reqHeight = wholeImageRect?.height() ?: return,
			) ?: return
			imageCropViewModel.onCapturedWholeImage(bitmap)
		}
	}

	private fun handleEvent(event: ImageCropUiEvent) {
		when (event) {
			is ImageCropUiEvent.MoveToGallery -> moveToGallery()
			is ImageCropUiEvent.CropSelectedImageArea -> cropSelectedImageArea(
				width = event.width,
				height = event.height
			)

			is ImageCropUiEvent.FinishWithCroppedImage -> finishWithCroppedImage(event.croppedImageBitmap)
			ImageCropUiEvent.RotateImageToRight -> rotateImageToRight()
			ImageCropUiEvent.CaptureWholeImage -> captureWholeImage()
		}
	}

	companion object {
		private const val LAUNCHER_INPUT_IMAGE = "image/*"
		private const val ANGLE_90 = 90
		const val EXTRA_CROPPED_IMAGE_CACHE_URI = "EXTRA_CROPPED_IMAGE_CACHE_URI"
		const val EXTRA_CROP_PURPOSE = "EXTRA_CROP_PURPOSE"
	}
}