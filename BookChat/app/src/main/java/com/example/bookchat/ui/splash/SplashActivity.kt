package com.example.bookchat.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.login.LoginActivity
import com.example.bookchat.ui.splash.SplashViewModel.SplashEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

//TODO : SplashScreen 적용
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
	private val splashViewModel: SplashViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		splashViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun moveToMain() {
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	private fun moveToLogin() {
		startActivity(Intent(this, LoginActivity::class.java))
		finish()
	}

	private fun handleEvent(event: SplashEvent) = when (event) {
		is SplashEvent.MoveToMain -> moveToMain()
		is SplashEvent.MoveToLogin -> moveToLogin()
	}
}