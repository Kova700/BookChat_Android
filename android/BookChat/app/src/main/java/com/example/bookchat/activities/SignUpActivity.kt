package com.example.bookchat.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var imm :InputMethodManager
    private var isAvailableNickname = false
    private var isNotDuplicateNickname = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        with(binding){
            activity = this@SignUpActivity
        }
        setEditText()
        setFocus()
    }

    private fun setEditText(){
        val specialFilter = InputFilter{ source, _, _, _, _, _ ->//
            val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
            val pattern = Pattern.compile(regex)
            if (source.isNullOrBlank() || pattern.matcher(source).matches()){
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
            nickNameEt.addTextChangedListener { lengthCheck() }
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
            isAvailableNickname = false
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
        isAvailableNickname = true
    }

    fun clickStartBtn(){
        if(isAvailableNickname){
            duplicateCheck()
            if (isNotDuplicateNickname){
                //정보 임시 저장하고
                //독서 취향 선택 페이지로 정보 넘겨주고
                //페이지 이동
            }
           return
        }
        lengthCheck()
    }

    //과도한 요청 보낼시 프론트 단에서 막을까?(서버에서 막는게 좋을듯)
    fun duplicateCheck() {
        //서버한테 중복 검사 요청

        //닉네임 사용가능 여부가 True 라면
        isNotDuplicateNickname = true
        with(binding){
            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_blue,null)
            checkResultTv.setTextColor(Color.parseColor("#5648FF"))
            checkResultTv.text = "사용 가능한 닉네임입니다."
        }

        //닉네임 사용가능 여부가 False 라면
        isNotDuplicateNickname = false
        with(binding){
            nickNameLayout.background = ResourcesCompat.getDrawable(resources,R.drawable.nickname_input_back_red,null)
            checkResultTv.setTextColor(Color.parseColor("#FF004D"))
            checkResultTv.text = "이미 사용 중인 닉네임 입니다."
        }
    }

    fun clickBackBtn() {
        finish()
    }
}