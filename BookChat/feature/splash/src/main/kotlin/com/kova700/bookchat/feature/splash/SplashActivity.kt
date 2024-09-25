package com.kova700.bookchat.feature.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.navigation.LoginActivityNavigator
import com.kova700.bookchat.core.navigation.MainNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kova700.bookchat.feature.splash.R as splashR

//TODO : SplashScreen 적용
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
	private val splashViewModel: SplashViewModel by viewModels()

	@Inject
	lateinit var loginNavigator: LoginActivityNavigator

	@Inject
	lateinit var mainNavigator: MainNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(splashR.layout.activity_splash)
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		splashViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun moveToMain() {
		mainNavigator.navigate(
			currentActivity = this,
			shouldFinish = true,
		)
	}

	private fun moveToLogin() {
		loginNavigator.navigate(
			currentActivity = this,
			shouldFinish = true,
		)
	}

	private fun handleEvent(event: SplashEvent) = when (event) {
		is SplashEvent.MoveToMain -> moveToMain()
		is SplashEvent.MoveToLogin -> moveToLogin()
	}
}