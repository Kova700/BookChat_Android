package com.example.bookchat.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
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

        startActivity(Intent(this,SelectTasteActivity::class.java))

        //일단 가입 api부터 만들고
//        if (isNotShort){
//            if (isNotDuplicate){
//                //인텐트에 유저 객체 실어야함
//                val intent = Intent(this,SelectTasteActivity::class.java)
//                tempUser
//                intent.putExtra("User",)
//                startActivity(intent) //유저 이름 가지고 페이지 이동해야함
//                //액티비티를 종료해야할까 말아야할까
//                return
//            }
//
//            //서버에게 중복성 검사하고 응답 받아야함
//            //응답에따라 "이미 사용중인 닉네임입니다." OR "사용 가능한 닉네임입니다.
//            //isNotDuplicate 갱신
//            duplicateCheck()
//        }


        //길이 검사통과했는지 체크하고
        //통과했으면 클릭 누르면 중복성 검사 하고
        //중복성검사 마저 통과했다면 다음페이지로 넘어가야함
        //텍스트 변경시 다시 중복검사 해야함

    }

    //과도한 요청 보낼시 서버에서 막음
    fun duplicateCheck() {
        //서버한테 중복 검사 요청

        //닉네임 사용가능 여부가 True 라면
        isNotDuplicate = true
        with(binding){
            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_blue,null)
            checkResultTv.setTextColor(Color.parseColor("#5648FF"))
            checkResultTv.text = "사용 가능한 닉네임입니다."
        }

        //닉네임 사용가능 여부가 False 라면
        isNotDuplicate = false
        with(binding){
            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
            checkResultTv.setTextColor(Color.parseColor("#FF004D"))
            checkResultTv.text = "이미 사용 중인 닉네임 입니다."
        }
    }

    fun clickProfileBtn(){

//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.setType("image/*")

        if (isPermissionGiven()){
            //권한 등록이 되어있다면 갤러리 진입
            Log.d(TAG, "SignUpActivity: clickProfileBtn() 권한체크 완료!(모든 권한을 가지고 있음)- called")
            return
        }
        //권한이 등록이 안되어있다면 권한 요청
        launchPermissions()
    }

    private fun isPermissionGiven(): Boolean{
        return (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
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

        if(deniedList.isNotEmpty()){
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
    }

    fun clickBackBtn() {
        finish()
    }
}