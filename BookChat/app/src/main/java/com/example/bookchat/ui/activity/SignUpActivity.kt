package com.example.bookchat.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookchat.R
import com.example.bookchat.data.UserSignUpRequestDto
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.repository.DupCheckRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.NameCheckStatus
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

class SignUpActivity : AppCompatActivity() {
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

        signUpViewModel.goSelectTasteActivity = {
//            val userEmail = withContext(lifecycleScope.coroutineContext) { getUserEmail() }
            val userProfilBitmap = binding.userProfileIv.drawable.toBitmap(300,300)

            val signUpDto = UserSignUpRequestDto(
                nickname = binding.nickNameEt.text.toString(), //데이터 체크
                defaultProfileImageType = 1, //임시
                userProfileImage = null,
            )
            val intent = Intent(this,SelectTasteActivity::class.java)
            intent.putExtra("signUpDto" , signUpDto)
            val byteArray = getByteArray(userProfilBitmap)
            intent.putExtra("userProfileImg",byteArray)
            //readingTastes, Img 입력 받아야함
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

    private fun getByteArray(bitmap :Bitmap) :ByteArray{
        return if(sdkVersionIsMoreR()) bitmapToByteArray(bitmap,Bitmap.CompressFormat.WEBP_LOSSLESS)
        else bitmapToByteArray(bitmap,Bitmap.CompressFormat.WEBP)
    }

    private fun bitmapToByteArray(bitmap: Bitmap, compressFormat :Bitmap.CompressFormat) :ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(compressFormat,100,stream) //임시 100
        return stream.toByteArray()
    }

    //WEBP 비손실 압축 버전 분기 목적
    private fun sdkVersionIsMoreR() :Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    suspend fun getUserEmail() :String{
        val userEmail = suspendCancellableCoroutine<String> { continuation ->
            UserApiClient.instance.me { user, error ->
                error?.let {
                    Log.d(TAG, "SignUpActivity: getUserEmail() - error : $error  - \"사용자 정보 요청 실패\"")
                }
                user?.let {
                    continuation.resume(user.kakaoAccount?.email!!)
                }
            }
        }
        Log.d(TAG, "SignUpActivity: getUserEmail() - userEmail : $userEmail")
        return userEmail
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
                    if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                }

                map["DENIED"]?.let{
                    Toast.makeText(this,"[사진 및 미디어]\n권한을 허용해주세요.",Toast.LENGTH_LONG).show()
                }

                map["EXPLAINED"]?.let {
                    Toast.makeText(this,"[권한] - [파일 및 미디어]\n권한을 허용해주세요.",Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    this.packageName.also { name ->
                        val uri = Uri.fromParts("package", name, null)
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

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                //크롭된 이미지 가져오기기
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra("image") ?: byteArrayOf()
                val bitmap = byteArrayToBitmap(bitmapByteArray)

                Glide.with(this)
                    .asBitmap()
                    .load(bitmap)
                    .into(binding.userProfileIv)
           }
        }

    private fun openGallery(){
        activityResultLauncher.launch("image/*")
    }

    private fun openCropActivity(uri :Uri){
        val intent = Intent(this, ImageCropActivity::class.java)
        intent.putExtra("uri",uri.toString())
        cropActivityResultLauncher.launch(intent)
    }

    private fun byteArrayToBitmap(byteArray: ByteArray) :Bitmap{
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    fun clickBackBtn() {
        finish()
    }
}