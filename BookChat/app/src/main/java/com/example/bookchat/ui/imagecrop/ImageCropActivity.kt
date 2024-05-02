package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import com.example.bookchat.utils.compressToByteArray

//TODO : 채팅방 이미지는 1:1 비율로하면 깨짐 (수정 가능하게 수정)
class ImageCropActivity : AppCompatActivity() {

	private lateinit var binding: ActivityImageCropBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
		binding.activity = this
		galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
	}

	private val galleryActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
			if (resultUri == null) {
				finish()
				return@registerForActivityResult
			}
			binding.cropImageView.setImageUriAsync(resultUri)
		}

	fun onClickFinishBtn() {
		val bitmap: Bitmap =
			binding.cropImageView.getCroppedImage(CROPPED_IMG_SIZE_WIDTH, CROPPED_IMG_SIZE_HEIGHT)!!
		val byteArray = bitmap.compressToByteArray()
		intent.putExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY, byteArray)
		setResult(RESULT_OK, intent)
		finish()
	}

	fun onClickCancelBtn() {
		finish()
	}

	fun onClickOtherPictureBtn() {
		galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
	}

	fun onClickRightRotatePictureBtn() {
		binding.cropImageView.rotateImage(90)
	}

	companion object {
		private const val CROPPED_IMG_SIZE_WIDTH = 200 //임시 200
		private const val CROPPED_IMG_SIZE_HEIGHT = 200 //임시 200
		const val EXTRA_CROPPED_PROFILE_BYTE_ARRAY = "EXTRA_CROPPED_PROFILE_BYTE_ARRAY"
		const val LAUNCHER_INPUT_IMAGE = "image/*"
	}
}