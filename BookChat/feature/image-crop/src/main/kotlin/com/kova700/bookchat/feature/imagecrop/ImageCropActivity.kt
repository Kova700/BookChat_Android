package com.kova700.bookchat.feature.imagecrop

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.image_crop.databinding.ActivityImageCropBinding
import com.kova700.bookchat.feature.imagecrop.model.ImageCropAspectRatio
import com.kova700.bookchat.util.image.image.saveImageToCacheAndGetUri
import kotlinx.coroutines.launch

class ImageCropActivity : AppCompatActivity() {

	private lateinit var binding: ActivityImageCropBinding

	private val imageCropViewModel: ImageCropViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityImageCropBinding.inflate(layoutInflater)
		setContentView(binding.root)
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

	private fun setCropImageViewState(uiState: ImageCropUiState) {
		setCropImageViewSize(uiState)
		setCropImageViewImageState(uiState)
		setCropImageViewCropOverlayState(uiState)
		setCropImageViewCropOverlayAspectRatio(uiState)
		setCaptureImageAspectRatioBtnState(uiState)
	}

	/**imgAspectRatioBtnLayout의 Visibility 상태를 gone -> visible 상태로 변경시
	 * CropImageView의 height가 자동 resizing 되지 않아서 수동으로 조절*/
	private fun setCropImageViewSize(uiState: ImageCropUiState) {
		val deviceHeightPx: Int = resources.displayMetrics.heightPixels
		val headerHeight = binding.imageCropHeader.height
		val footerHeight = if (uiState.isImageCropping.not()) 0 else
			resources.getDimension(R.dimen.image_crop_aspect_ratio_btn_layout_height).toInt()
		val newCropImageViewHeight = deviceHeightPx - (headerHeight + footerHeight)
		binding.cropImageView.layoutParams.height = newCropImageViewHeight
	}

	private fun setCaptureImageAspectRatioBtnState(uiState: ImageCropUiState) {
		with(binding) {
			imgAspectRatioBtnLayout.visibility =
				if (uiState.isImageCropping) View.VISIBLE else View.GONE
			with(imgAspectRatioBtn11) {
				isChecked = uiState.imageCropAspectRatio == ImageCropAspectRatio.RATIO_1_1
				isEnabled = isChecked.not()
			}
			with(imgAspectRatioBtn34) {
				isChecked = uiState.imageCropAspectRatio == ImageCropAspectRatio.RATIO_3_4
				isEnabled = isChecked.not()
			}
			with(imgAspectRatioBtn43) {
				isChecked = uiState.imageCropAspectRatio == ImageCropAspectRatio.RATIO_4_3
				isEnabled = isChecked.not()
			}
			with(imgAspectRatioBtnFree) {
				isChecked = uiState.imageCropAspectRatio == ImageCropAspectRatio.RATIO_FREE
				isEnabled = isChecked.not()
			}
		}
	}

	private fun setCropImageViewImageState(uiState: ImageCropUiState) {
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

	/** OverlayShape 스쿼클 모양으로 추후 업데이트
	 * (Oval 사용시 동그라미 크가 1대1 종횡비가 아니면 원 모양이 늘어나는 현상 때문에 Rectangle로 임시 통일) */
	private fun setCropImageViewCropOverlayState(uiState: ImageCropUiState) {
		with(binding.cropImageView) {
			isShowCropOverlay = uiState.isImageCropping
		}
	}

	private fun setCropImageViewCropOverlayAspectRatio(uiState: ImageCropUiState) {
		with(binding.cropImageView) {
			when (uiState.imageCropAspectRatio) {
				ImageCropAspectRatio.RATIO_FREE -> clearAspectRatio()
				ImageCropAspectRatio.RATIO_1_1 -> setAspectRatio(1, 1)
				ImageCropAspectRatio.RATIO_3_4 -> setAspectRatio(3, 4)
				ImageCropAspectRatio.RATIO_4_3 -> setAspectRatio(4, 3)
			}
		}
	}

	private fun initViewState() {
		with(binding) {
			backBtn.setOnClickListener { imageCropViewModel.onClickBackBtn() }
			cropCancelBtn.setOnClickListener { imageCropViewModel.onClickCropCancelBtn() }
			cropFinishBtn.setOnClickListener { imageCropViewModel.onClickCropFinishBtn() }
			finalConfirmBtn.setOnClickListener { imageCropViewModel.onClickFinalConfirmBtn() }
			imgCropBtn.setOnClickListener {
				cropImageView.wholeImageRect?.let { rect -> imageCropViewModel.onClickImageCropBtn(rect) }
			}
			imgAspectRatioBtn11.setOnClickListener {
				imageCropViewModel.onClickCaptureImageAspectRatioBtn(ImageCropAspectRatio.RATIO_1_1)
			}
			imgAspectRatioBtn34.setOnClickListener {
				imageCropViewModel.onClickCaptureImageAspectRatioBtn(ImageCropAspectRatio.RATIO_3_4)
			}
			imgAspectRatioBtn43.setOnClickListener {
				imageCropViewModel.onClickCaptureImageAspectRatioBtn(ImageCropAspectRatio.RATIO_4_3)
			}
			imgAspectRatioBtnFree.setOnClickListener {
				imageCropViewModel.onClickCaptureImageAspectRatioBtn(ImageCropAspectRatio.RATIO_FREE)
			}
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