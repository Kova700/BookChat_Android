package com.example.bookchat.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.repository.DupCheckRepository
import com.example.bookchat.utils.Constants.TAG
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var imm :InputMethodManager
    private var isNotShort = false
    private var isNotDuplicate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        with(binding){
            activity = this@SignUpActivity
        }
        setEditText()
        setFocus()
        binding.userProfileIv.clipToOutline = true
        //onActivityResult로 갤러리에서 이미지 가져와야함
    }

    //이거 바인딩 어뎁터로 설정이 가능할거 같은데?
    private fun setEditText(){
        val specialFilter = InputFilter{ source, _, _, _, _, _ ->
            val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
            val pattern = Pattern.compile(regex)
            if (pattern.matcher(source).matches()){
                return@InputFilter source //통과되는 문자일 때 (입력값 그대로 출력)
            }
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
                isNotDuplicate = false //검사 다시해야함
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
        if (isNotShort){
            duplicateCheck() //사용가능을 받아놓은 사이에 다른 유저가 닉네임을 가져갈 수 있음으로 누를때마다 검사하는게 맞을듯
            if (isNotDuplicate){  //비동기 속도차이에 의해 최초엔 false로 인식하고 작동 안함
                //인텐트에 유저 객체 실어야함
                val intent = Intent(this,SelectTasteActivity::class.java)
//                tempUser
//                intent.putExtra("User",)
                startActivity(intent) //유저 이름 가지고 페이지 이동해야함
                //액티비티를 종료해야할까 말아야할까
                return
            }
        }
    }

    fun duplicateCheck() {
        //서버한테 중복 검사 요청
        val repository = DupCheckRepository()
        repository.duplicateCheck { dupCheckResult ->
            when(dupCheckResult){
                true -> {
                    //닉네임 사용가능 여부가 True 라면
                    isNotDuplicate = true
                    with(binding){
                        nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_blue,null)
                        checkResultTv.setTextColor(Color.parseColor("#5648FF"))
                        checkResultTv.text = "사용 가능한 닉네임입니다."
                    }
                }
                false ->{
                    //닉네임 사용가능 여부가 False 라면
                    isNotDuplicate = false
                    with(binding){
                        nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
                        checkResultTv.setTextColor(Color.parseColor("#FF004D"))
                        checkResultTv.text = "이미 사용 중인 닉네임 입니다."
                    }
                }
            }
            Log.d(TAG, "SignUpActivity: duplicateCheck() - called")
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