package com.example.bookchat.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.kakao.KakaoSDK
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        loginViewModel = ViewModelProvider(this, ViewModelFactory()).get(LoginViewModel::class.java)
        loginViewModel.loginSuccessCallBack = { startActivity(Intent(this,SignUpActivity::class.java)) }

        with(binding){
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
            viewModel = loginViewModel
        }

        //자동로그인
        //DataStore에 토큰타입이 저장되어 있다면 해당 토큰으로 자동 로그인 시도

//        lifecycleScope.launch {
//            checkToken()
//        }

    }

//    private suspend fun checkToken() {
//        if (KakaoSDK.isAvailableToken()){
//            startActivity(Intent(this,MainActivity::class.java))
//            finish()
//        }
//    }

}