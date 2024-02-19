package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.ui.viewmodel.SplashViewModel
import com.example.bookchat.ui.viewmodel.SplashViewModel.SplashEvent
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
        },SPLASH_DURATION)
    }

    //onNewToken에 로컬DB에 FCM토큰을 저장하는 로직을 넣어둔다.
    //SplashActivity에서 FCM토큰을 매번 가져와서 로컬DB에 저장된 FCM토큰과 같은 토큰인지 확인한다.
    //두 토큰이 서로 다르다면 새로 받은 토큰을 로컬 DB에 저장하고, 새로 저장한 FCM토큰을 서버로 보내 FCM토큰을 업데이트한다.
    //두 토큰이 서로 같다면 토큰이 변경되지 않았음으로, 다시 일반적인 로직으로 돌아간다. (토큰이 유효하면 MainActivity등..)

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