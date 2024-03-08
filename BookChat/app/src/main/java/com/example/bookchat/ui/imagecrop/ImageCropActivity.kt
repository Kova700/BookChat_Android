package com.example.bookchat.ui.imagecrop

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import java.io.ByteArrayOutputStream

class ImageCropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        binding.activity = this
        galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { resultUri ->
        if (resultUri == null) {
            finish()
            return@registerForActivityResult
        }
        binding.cropImageView.setImageUriAsync(resultUri)
    }

    fun clickFinishBtn() {
        val bitmap: Bitmap =
            binding.cropImageView.getCroppedImage(CROPPED_IMG_SIZE_WIDTH, CROPPED_IMG_SIZE_HEIGHT)!!
        val byteArray = getByteArray(bitmap)
        intent.putExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY, byteArray)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun clickCancelBtn() {
        finish()
    }

    fun clickOtherPictureBtn() {
        galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
    }

    fun clickRightRotatePictureBtn() {
        binding.cropImageView.rotateImage(90)
    }

    private fun bitmapToByteArray(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(compressFormat, BITMAP_COMPRESS_QUALITY, stream)
        return stream.toByteArray()
    }

    private fun getByteArray(bitmap: Bitmap): ByteArray {
        if (sdkVersionIsMoreR()) {
            return bitmapToByteArray(bitmap, Bitmap.CompressFormat.WEBP_LOSSLESS)
        }
        return bitmapToByteArray(bitmap, Bitmap.CompressFormat.WEBP)
    }

    private fun sdkVersionIsMoreR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    companion object {
        private const val CROPPED_IMG_SIZE_WIDTH = 200 //임시 200
        private const val CROPPED_IMG_SIZE_HEIGHT = 200 //임시 200
        private const val BITMAP_COMPRESS_QUALITY = 80 //임시 80
        const val EXTRA_CROPPED_PROFILE_BYTE_ARRAY = "EXTRA_USER_PROFILE_BYTE_ARRAY1"
        const val LAUNCHER_INPUT_IMAGE = "image/*"
    }
}