package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import com.example.bookchat.utils.image.bitmap.compressToByteArray
import kotlinx.coroutines.launch

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
		binding.cropImageView.setImageUriAsync(uiState.selectedImageUrl)
	}

	private fun initViewState() {
		with(binding) {
			cancelBtn.setOnClickListener { imageCropViewModel.onClickCancelBtn() }
			imgRotateBtn.setOnClickListener { imageCropViewModel.onClickRightRotatePictureBtn() }
			imageSelectBtn.setOnClickListener { imageCropViewModel.onClickMoveToGalleryBtn() }
			confirmBtn.setOnClickListener { imageCropViewModel.onClickFinishBtn() }
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

	private fun rotateImageToTheRight() {
		binding.cropImageView.rotateImage(90)
	}

	private fun cropSelectedImageArea(width: Int, height: Int) {
		val bitmap: Bitmap =
			binding.cropImageView.getCroppedImage(
				reqWidth = width,
				reqHeight = height
			) ?: return
		imageCropViewModel.onChangeCroppedImage(bitmap.compressToByteArray())
	}

	private fun finishWithCroppedImage(croppedImage: ByteArray) {
		intent.putExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY, croppedImage)
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

	private fun handleEvent(event: ImageCropUiEvent) {
		when (event) {
			is ImageCropUiEvent.MoveToBack -> finish()
			is ImageCropUiEvent.MoveToGallery -> moveToGallery()
			is ImageCropUiEvent.CropSelectedImageArea -> cropSelectedImageArea(
				width = event.width,
				height = event.height
			)

			is ImageCropUiEvent.FinishWithCroppedImage -> finishWithCroppedImage(event.croppedImage)
			ImageCropUiEvent.RotateImageToRight -> rotateImageToTheRight()
		}
	}

	companion object {
		const val EXTRA_CROPPED_PROFILE_BYTE_ARRAY = "EXTRA_CROPPED_PROFILE_BYTE_ARRAY"
		const val LAUNCHER_INPUT_IMAGE = "image/*"
	}
}