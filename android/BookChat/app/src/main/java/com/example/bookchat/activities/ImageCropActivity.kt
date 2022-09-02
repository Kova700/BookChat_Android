package com.example.bookchat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityImageCropBinding
import com.example.bookchat.utils.Constants.TAG
import java.io.ByteArrayOutputStream

class ImageCropActivity : AppCompatActivity() {
    private lateinit var binding : ActivityImageCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        binding.activity = this

        val uri = intent.getStringExtra("uri")!!.toUri()
        binding.cropImageView.setImageUriAsync(uri)

    }

    @RequiresApi(Build.VERSION_CODES.R) //버전 분기 필요
    fun clickFinishBtn(){
        Log.d(TAG, "ImageCropActivity: clickFinishBtn() - called")
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap :Bitmap = binding.cropImageView.getCroppedImage(500,500)!! //사이즈 조정필요
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS,80,byteArrayOutputStream)
        val intent = Intent(this@ImageCropActivity,SignUpActivity::class.java)
        intent.putExtra("bitmap",byteArrayOutputStream.toString().toByteArray())
        setResult(1,intent)
        finish()
    }

}