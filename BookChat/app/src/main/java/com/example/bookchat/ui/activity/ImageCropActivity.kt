package com.example.bookchat.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import com.example.bookchat.ui.activity.SignUpActivity.Companion.EXTRA_USER_PROFILE_URI
import com.example.bookchat.utils.Constants.TAG
import java.io.ByteArrayOutputStream

class ImageCropActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER_PROFILE_BYTE_ARRAY1 = "EXTRA_USER_PROFILE_BYTE_ARRAY1"
    }

    private lateinit var binding : ActivityImageCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        binding.activity = this

        val uri = intent.getStringExtra(EXTRA_USER_PROFILE_URI)!!.toUri()
        binding.cropImageView.setImageUriAsync(uri)

    }

    fun clickFinishBtn(){
        Log.d(TAG, "ImageCropActivity: clickFinishBtn() - called")
        val bitmap :Bitmap = binding.cropImageView.getCroppedImage(200,200)!!
        val byteArray = getByteArray(bitmap)
        val intent = Intent(this@ImageCropActivity, SignUpActivity::class.java)
        intent.putExtra(EXTRA_USER_PROFILE_BYTE_ARRAY1,byteArray)
        setResult(RESULT_OK,intent)
        finish()
    }

    private fun bitmapToByteArray(bitmap: Bitmap, compressFormat :Bitmap.CompressFormat) :ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(compressFormat,80,stream) //임시 80
        return stream.toByteArray()
    }

    private fun getByteArray(bitmap :Bitmap) :ByteArray{
        return if(sdkVersionIsMoreR()) bitmapToByteArray(bitmap,Bitmap.CompressFormat.WEBP_LOSSLESS)
        else bitmapToByteArray(bitmap,Bitmap.CompressFormat.WEBP)
    }

    private fun sdkVersionIsMoreR() :Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

}