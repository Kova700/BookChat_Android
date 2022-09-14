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
import android.text.InputFilter
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookchat.R
import com.example.bookchat.data.UserSignUpRequestDto
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.repository.DupCheckRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.viewmodel.SelectTasteViewModel
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import kotlin.coroutines.resume

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var signUpViewModel : SignUpViewModel
    private lateinit var imm :InputMethodManager
    private var isNotShort = false
    private var isNotDuplicate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        signUpViewModel = ViewModelProvider(this, ViewModelFactory()).get(SignUpViewModel::class.java)
        with(binding){
            lifecycleOwner = this@SignUpActivity
            activity = this@SignUpActivity
            viewModel = signUpViewModel
        }
        setEditText()
        setFocus()
        binding.userProfileIv.clipToOutline = true // 이거 기능 확인해보자.
    }

    //이거 바인딩 어뎁터로 설정이 가능할거 같은데?
    //혹은 ViewModel로 UI 설정 가능
    private fun setEditText(){
        val specialFilter = InputFilter{ source, _, _, _, _, _ ->
            val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
            val pattern = Pattern.compile(regex)
            if (pattern.matcher(source).matches()){
                return@InputFilter source //통과되는 문자일 때 (입력값 그대로 출력)
            }
            signUpViewModel._isSpecialCharInText.value = false
            //특수문자 입력시 UI 수정해야함 ( 레이아웃 테두리, 텍스트뷰 ) <= 이놈들 객체를 어떻게 뽑지
            with(binding){
                nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
                checkResultTv.setTextColor(Color.parseColor("#FF004D"))
                checkResultTv.text = "특수문자는 사용 불가능합니다."
            }
            "" //통과되지 않는 문자일 때 대체 문자
        }

        with(binding){
            nickNameEt.filters = arrayOf(specialFilter)
            nickNameEt.addTextChangedListener {
                //글자가 수정될때마다 중복, 길이 검사 초기화
                isNotDuplicate = false
                lengthCheck()
            }
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

    private fun lengthCheck(){
        val inputedText = binding.nickNameEt.text.toString()
        if (inputedText.length < 2){
            isNotShort = false
            with(binding){
                nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
                checkResultTv.setTextColor(Color.parseColor("#FF004D"))
                checkResultTv.text = "최소 2자리 이상 입력해 주세요."
            }
            return
        }
        with(binding){
            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_white,null)
            checkResultTv.text = ""
        }
        isNotShort = true
    }

    fun clickStartBtn(){
        lifecycleScope.launch{
            if (isNotShort){
                if (isNotDuplicate){
                    goSelectTasteActivity()
                    return@launch
                }
                duplicateCheck()
            }
            lengthCheck()
        }
    }

    private suspend fun goSelectTasteActivity(){
        val userEmail = withContext(lifecycleScope.coroutineContext) { getUserEmail() }
        val userProfilBitmap = binding.userProfileIv.drawable.toBitmap(300,300)

        val signUpDto = UserSignUpRequestDto(
            nickname = binding.nickNameEt.text.toString(), //데이터 체크
            userEmail = userEmail,
            oauth2Provider = "kakao", //임시
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
        //나중에 객체 생성하는 부분 다 싱글톤으로 정리하기 (그냥 Object로 KAKAOSDK 만들면 안되나?)
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

    //서버한테 중복 검사 요청
    suspend fun duplicateCheck() {
        Log.d(TAG, "SignUpActivity: duplicateCheck() - called - isNotDuplicate : $isNotDuplicate")
        val repository = DupCheckRepository() //이거도 싱글톤으로 뭔가 수정해야할 느낌
        isNotDuplicate = suspendCancellableCoroutine<Boolean> { continuation ->
            repository.duplicateCheck { dupCheckResult ->
                when(dupCheckResult){
                    true -> {
                        //닉네임 사용가능 여부가 True 라면
                        with(binding){
                            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_blue,null)
                            checkResultTv.setTextColor(Color.parseColor("#5648FF"))
                            checkResultTv.text = "사용 가능한 닉네임입니다."
                        }
                        continuation.resume(true)
                    }
                    false ->{
                        //닉네임 사용가능 여부가 False 라면
                        with(binding){
                            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
                            checkResultTv.setTextColor(Color.parseColor("#FF004D"))
                            checkResultTv.text = "이미 사용 중인 닉네임 입니다."
                        }
                        continuation.resume(false)
                    }
                }
                Log.d(TAG, "SignUpActivity: duplicateCheck() - 검사 완료 - isNotDuplicate : $isNotDuplicate")
            }
        }
    }

    fun clickProfileBtn(){
        launchPermissions()
    }

    private fun launchPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissions.launch(permissions) //권한 검사 및 요청
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

    val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
            openCropActivity(resultUri!!)
        }

    val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                //크롭된 이미지 가져오기기
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra("image") ?: byteArrayOf()
                val bitmap = byteArrayToBitmap(bitmapByteArray)
                //유저 객체에 bitmap 실어야함

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