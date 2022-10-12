package com.example.bookchat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.data.UserSignUpRequestDto
import com.example.bookchat.databinding.ActivitySelectTasteBinding
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.SelectTasteViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

class SelectTasteActivity : AppCompatActivity() {
    private lateinit var binding :ActivitySelectTasteBinding
    private lateinit var selectTasteViewModel: SelectTasteViewModel
    private lateinit var signUpUserDto :UserSignUpRequestDto
    private lateinit var userProfilBitmap : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_taste)
        selectTasteViewModel = ViewModelProvider(this,ViewModelFactory()).get(SelectTasteViewModel::class.java)
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
        signUpUserDto = intent.getSerializableExtra("signUpDto") as UserSignUpRequestDto
        Log.d(TAG, "SelectTasteActivity: getSignUpUserDtoFromIntent() - signUpUserDto : $signUpUserDto")
    }

    private fun getSignUpUserProfileImgFromIntent(){
        val bitmapByteArray = intent?.getByteArrayExtra("userProfileImg") ?: byteArrayOf()
        userProfilBitmap = byteArrayToBitmap(bitmapByteArray)
    }

    private fun getFinalSignUpUserDto(){
        val bitmapMultipartBody = BitmapToRequestBody(userProfilBitmap)
        val userProfileImageMultiPart =  MultipartBody.Part.createFormData("userProfileImg","profle_img",bitmapMultipartBody)
        signUpUserDto.userProfileImage = userProfileImageMultiPart
        Log.d(TAG, "SelectTasteActivity: getFinalSignUpUserDto() - signUpUserDto : $signUpUserDto")
    }


    //이미지 확장자 WEBP , 압축도 80 임시로 설정
    inner class BitmapToRequestBody(private val bitmap: Bitmap) : RequestBody() {
        override fun contentType(): MediaType? {
            return MediaType.parse("image/webp")
        }

        override fun writeTo(sink: BufferedSink) {
//            if(sdkVersionIsMoreR()) bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 80, sink.outputStream())
//            else bitmap.compress(Bitmap.CompressFormat.WEBP, 80, sink.outputStream())
            sink.outputStream() //이미 한번 압축을 했기때문에 굳이 여기서 한 번 더 안해도 될듯
        }

    }

    private fun byteArrayToBitmap(byteArray: ByteArray) :Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    fun clickBackBtn() {
        finish()
    }

    fun clickFinshBtn() {
        // userRepository = UserRepository() //임시
        //이거 객체 만들어 졌는지 안만들어졌는지 확인하고 보낼 수 있게 동시성 정리
        //userRepository.signUp({ Log.d(TAG, "SelectTasteActivity: 회원가입 통신")},signUpUserDto)
        val intent = Intent(this,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK //새로운 태스크 생성
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // 실행 액티비티 외 모두 제거
        startActivity(intent)
    }
}