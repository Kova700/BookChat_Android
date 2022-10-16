package com.example.bookchat.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

const val DENIED = "DENIED"
const val EXPLAINED = "EXPLAINED"
const val SCHEME_PACKAGE = "package"
const val LAUNCHER_INPUT_IMAGE = "image/*"

class SignUpActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
        const val EXTRA_SIGNUP_DTO = "EXTRA_SIGNUP_DTO"
        const val EXTRA_USER_PROFILE_BYTE_ARRAY2 = "EXTRA_USER_PROFILE_BYTE_ARRAY2"
    }

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var signUpViewModel : SignUpViewModel
    private lateinit var imm :InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        signUpViewModel = ViewModelProvider(this, ViewModelFactory()).get(SignUpViewModel::class.java)
        with(binding){
            lifecycleOwner = this@SignUpActivity
            activity = this@SignUpActivity
            viewModel = signUpViewModel
        }
        setFocus()

        //회원가입(프로필설정페이지)에서 다음페이지로 넘어갈 콜백 메서드 지정 중 (임시)
        signUpViewModel.goSelectTasteActivity = {
            val signUpDto = signUpViewModel._signUpDto.value
            val byteArray = signUpViewModel._userProfilByteArray.value
            val intent = Intent(this, SelectTasteActivity::class.java)
            intent.putExtra(EXTRA_SIGNUP_DTO , signUpDto)
            intent.putExtra(EXTRA_USER_PROFILE_BYTE_ARRAY2,byteArray)
            startActivity(intent)
        }

    }

    private fun setFocus(){
        binding.nickNameEt.requestFocus()
        openKeyboard()
    }

    private fun openKeyboard() {
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Handler(Looper.getMainLooper()).postDelayed({
            imm.showSoftInput(binding.nickNameEt,0)
        },200)
    }

    fun clickProfileBtn(){
        launchPermissions()
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result: Map<String, Boolean> -> //권한별 결과값 Boolean값으로 가짐

        val deniedList = result.filter { !it.value }.map { it.key }

        when{
            deniedList.isNotEmpty() -> {
                //명시적 거부 -> DENIED , 다시 묻지 않음(두 번 거부) -> EXPLAINED
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED
                }

                map[DENIED]?.let{
                    Toast.makeText(this,R.string.message_permission_DENIED,Toast.LENGTH_LONG).show()
                }

                map[EXPLAINED]?.let {
                    Toast.makeText(this,R.string.message_permission_EXPLAINED,Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    this.packageName.also { name ->
                        val uri = Uri.fromParts(SCHEME_PACKAGE, name, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }
            }
            //모든 권한 허용 확인
            else -> openGallery()
        }
    }

    private fun launchPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissions.launch(permissions) //권한 검사 및 요청
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
            openCropActivity(resultUri!!)
        }

    private fun openGallery(){
        activityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                //크롭된 이미지 가져오기기
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra(EXTRA_USER_PROFILE_BYTE_ARRAY1) ?: byteArrayOf()
                signUpViewModel._userProfilByteArray.value = bitmapByteArray
                val bitmap = byteArrayToBitmap(bitmapByteArray)

                Glide.with(this)
                    .asBitmap()
                    .load(bitmap)
                    .into(binding.userProfileIv)
            }
        }

    private fun openCropActivity(uri :Uri){
        val intent = Intent(this, ImageCropActivity::class.java)
        intent.putExtra(EXTRA_USER_PROFILE_URI,uri.toString())
        cropActivityResultLauncher.launch(intent)
    }

    private fun byteArrayToBitmap(byteArray: ByteArray) :Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    fun clickBackBtn() {
        finish()
    }
}