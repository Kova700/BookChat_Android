package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.databinding.ActivitySelectTasteBinding
import com.example.bookchat.ui.activity.SignUpActivity.Companion.EXTRA_SIGNUP_DTO
import com.example.bookchat.ui.activity.SignUpActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY2
import com.example.bookchat.viewmodel.SelectTasteViewModel
import com.example.bookchat.viewmodel.SelectTasteViewModel.SelectTasteEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

@AndroidEntryPoint
class SelectTasteActivity : AppCompatActivity() {

    private lateinit var binding :ActivitySelectTasteBinding
    private val selectTasteViewModel: SelectTasteViewModel by viewModels()
    private lateinit var signUpUserDto :UserSignUpDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_taste)

        with(binding){
            lifecycleOwner = this@SelectTasteActivity
            activity = this@SelectTasteActivity
            viewModel = selectTasteViewModel
        }

        lifecycleScope.launch {
            selectTasteViewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        getSignUpUserDtoFromIntent()
        getSignUpUserProfileImgFromIntent()
    }

    private fun getSignUpUserDtoFromIntent(){
        signUpUserDto = intent.getSerializableExtra(EXTRA_SIGNUP_DTO) as UserSignUpDto
        selectTasteViewModel._signUpDto.value = signUpUserDto
    }

    private fun getSignUpUserProfileImgFromIntent(){
        val bitmapByteArray = intent?.getByteArrayExtra(EXTRA_USER_PROFILE_BYTE_ARRAY2)
        val imageMultipartBody = bitmapByteArray?.let { byteArrayToRequestBody(it) }
        val userProfileImageMultiPart = imageMultipartBody?.let {
            MultipartBody.Part.createFormData(IMAGE_MULTIPART_NAME,IMAGE_FILE_NAME + IMAGE_FILE_EXTENSION, it)
        }
        signUpUserDto.userProfileImage = userProfileImageMultiPart
    }

    private fun byteArrayToRequestBody(byteArray : ByteArray) :RequestBody{
        return RequestBody.create(CONTENT_TYPE_IMAGE_WEBP.toMediaTypeOrNull(),byteArray)
    }

    private fun handleEvent(event: SelectTasteEvent) = when(event) {
        is SelectTasteEvent.Forbidden -> { Snackbar.make(binding.selectTasteLayout,R.string.login_forbidden_user, Snackbar.LENGTH_SHORT).show() }
        is SelectTasteEvent.NetworkError -> { Snackbar.make(binding.selectTasteLayout,R.string.error_network, Snackbar.LENGTH_SHORT).show() }
        is SelectTasteEvent.UnknownError -> { Snackbar.make(binding.selectTasteLayout,R.string.error_else, Snackbar.LENGTH_SHORT).show() }
        is SelectTasteEvent.MoveToBack -> { finish() }
        is SelectTasteEvent.MoveToMain -> {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    companion object{
        const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
        const val IMAGE_FILE_NAME = "profile_img"
        const val IMAGE_FILE_EXTENSION = ".webp"
        const val IMAGE_MULTIPART_NAME = "userProfileImage"
    }

}