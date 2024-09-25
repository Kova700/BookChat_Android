package com.kova700.bookchat.feature.splash

sealed class SplashEvent {
	data object MoveToMain : SplashEvent()
	data object MoveToLogin : SplashEvent()
}
