package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        loginViewModel = ViewModelProvider(this, ViewModelFactory()).get(LoginViewModel::class.java)
        loginViewModel.goMainActivityCallBack = { startActivity(Intent(this, MainActivity::class.java)); finish() }
        loginViewModel.goSignUpActivityCallBack = { startActivity(Intent(this, SignUpActivity::class.java)); finish() }

        with(binding){
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
            viewModel = loginViewModel
        }
        //회원가입 성공하면 메인 페이지로 이동하는 로직
        //토큰 갱신완료하면 다시 돌아가는 로직
        //완성해야함
//        loginViewModel.goSignUpActivityCallBack()
//        loginViewModel.goMainActivityCallBack()
    }

    fun startKakoLogin(){
        loginViewModel.startKakaoLogin(this)
    }


}