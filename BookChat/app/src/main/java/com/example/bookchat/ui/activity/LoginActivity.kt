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

    private fun showSnackbar(textId :Int){
        Snackbar.make(binding.loginLayout, textId, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleEvent(event: LoginEvent) = when(event) {
        is LoginEvent.MoveToMain -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
        is LoginEvent.MoveToSignUp -> { startActivity(Intent(this, SignUpActivity::class.java)); }
        is LoginEvent.Forbidden -> { showSnackbar(R.string.login_forbidden_user) }
        is LoginEvent.NetworkError -> { showSnackbar(R.string.error_network) }
        is LoginEvent.KakaoLoginFail -> { showSnackbar(R.string.error_kakao_login) }
        is LoginEvent.UnknownError -> { showSnackbar(R.string.error_else)}
    }

}