package com.example.bookchat.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.kakao.KakaoSDK
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.LoginType
import com.example.bookchat.utils.SharedPreferenceManager
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
        loginViewModel.startKakaoLogin = { lifecycleScope.launch{ KakaoSDK.login() } }
        loginViewModel.loginSuccessCallBack = { startActivity(Intent(this,SignUpActivity::class.java)) }

        with(binding){
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
            viewModel = loginViewModel
        }
        checkToken()
    }

    private fun checkToken() {

        //현재 토큰 확인하고 만료되지 않은 토큰있으면 북챗로그인 시도
        //토큰이 없으면 무반응
            //로그인 버튼 누르면 SDK 통해서 토큰 받아서 로그인 시도
    }

}