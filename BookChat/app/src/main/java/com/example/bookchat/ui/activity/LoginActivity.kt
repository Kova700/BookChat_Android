package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.LoginViewModel.LoginEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var loginViewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        loginViewModel = ViewModelProvider(this, ViewModelFactory()).get(LoginViewModel::class.java)

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
        is LoginEvent.UnknownError -> { Snackbar.make(binding.loginLayout,R.string.message_error_else,Snackbar.LENGTH_SHORT).show() }
    }

}