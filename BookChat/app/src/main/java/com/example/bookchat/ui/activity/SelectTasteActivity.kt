package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.databinding.ActivitySelectTasteBinding
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.SelectTasteViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SelectTasteActivity : AppCompatActivity() {
    private lateinit var binding :ActivitySelectTasteBinding
    private lateinit var selectTasteViewModel: SelectTasteViewModel
    private lateinit var signUpUserDto :UserSignUpDto
    private var userProfileByteArray :ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_taste)
        selectTasteViewModel = ViewModelProvider(this,ViewModelFactory()).get(SelectTasteViewModel::class.java)
        selectTasteViewModel.goMainActivityCallBack = {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK //새로운 태스크 생성
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // 실행 액티비티 외 모두 제거
            startActivity(intent)
        }
        with(binding){
            lifecycleOwner = this@SelectTasteActivity
            activity = this@SelectTasteActivity
            viewModel = selectTasteViewModel
        }
        getSignUpUserDtoFromIntent()
        getSignUpUserProfileImgFromIntent()
        getFinalSignUpUserDto()
    }

    private fun getSignUpUserDtoFromIntent(){
        signUpUserDto = intent.getSerializableExtra("signUpDto") as UserSignUpDto
        Log.d(TAG, "SelectTasteActivity: getSignUpUserDtoFromIntent() - signUpUserDto : $signUpUserDto")
    }

    private fun getSignUpUserProfileImgFromIntent(){
        val bitmapByteArray = intent?.getByteArrayExtra("userProfileImg")
        bitmapByteArray?.let { userProfileByteArray = bitmapByteArray }
    }

    private fun getFinalSignUpUserDto(){
        val imageMultipartBody = userProfileByteArray?.let { byteArrayToRequestBody(it) }
        val userProfileImageMultiPart = imageMultipartBody?.let {
            MultipartBody.Part.createFormData("userProfileImg","profle_img", it)
        }
        signUpUserDto.userProfileImage = userProfileImageMultiPart
        selectTasteViewModel._signUpDto.value = signUpUserDto //임시
        Log.d(TAG, "SelectTasteActivity: getFinalSignUpUserDto() - signUpUserDto : $signUpUserDto")
    }

    private fun byteArrayToRequestBody(byteArray : ByteArray) :RequestBody{
        return RequestBody.create(MediaType.parse("image/webp"),byteArray)
    }

    fun clickBackBtn() {
        finish()
    }

}