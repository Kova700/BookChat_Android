package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.LoginViewModel.LoginEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private val loginViewModel : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        with(binding){
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
            viewModel = loginViewModel
        }

        lifecycleScope.launch {
            loginViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    fun startKakoLogin(){
        loginViewModel.startKakaoLogin(this)
    }

    private fun handleEvent(event: LoginEvent) = when(event) {
        is LoginEvent.MoveToMain -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
        is LoginEvent.MoveToSignUp -> { startActivity(Intent(this, SignUpActivity::class.java)); }
        is LoginEvent.Forbidden -> { Snackbar.make(binding.loginLayout,R.string.message_forbidden,Snackbar.LENGTH_SHORT).show() }
        is LoginEvent.NetworkError -> { Snackbar.make(binding.loginLayout,R.string.message_error_network,Snackbar.LENGTH_SHORT).show() }
        is LoginEvent.KakaoLoginFail -> { Snackbar.make(binding.loginLayout,R.string.message_kakao_login_fail,Snackbar.LENGTH_SHORT).show() }
        is LoginEvent.UnknownError -> { Snackbar.make(binding.loginLayout,R.string.message_error_else,Snackbar.LENGTH_SHORT).show() }
    }

}