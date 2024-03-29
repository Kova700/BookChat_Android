package com.example.bookchat.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.login.LoginActivity
import com.example.bookchat.ui.splash.SplashViewModel.SplashEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val splashViewModel : SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            observeUiEvent()
        }, SPLASH_DURATION)
    }

    private fun observeUiEvent() = lifecycleScope.launch {
        splashViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun handleEvent(event: SplashEvent) = when(event) {
        is SplashEvent.MoveToMain -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
        is SplashEvent.MoveToLogin -> { startActivity(Intent(this, LoginActivity::class.java)); finish() }
    }

    companion object{
        const val SPLASH_DURATION = 1500L
    }
}