package com.example.bookchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.example.bookchat.databinding.ActivityImageCropBinding

class ImageCropActivity : AppCompatActivity() {
    private lateinit var binding : ActivityImageCropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        val uri = intent.getStringExtra("uri")!!.toUri()
        binding.cropImageView.setImageUriAsync(uri)
//        setCropImageView()
    }

}