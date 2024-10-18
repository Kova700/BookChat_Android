package com.kova700.bookchat.feature.login

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.data.oauth.external.model.OAuth2Provider
import com.kova700.bookchat.core.navigation.MainNavigator
import com.kova700.bookchat.core.navigation.SignUpActivityNavigator
import com.kova700.bookchat.core.oauth.external.OAuthClient
import com.kova700.bookchat.feature.login.databinding.ActivityLoginBinding
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
	private lateinit var binding: ActivityLoginBinding
	private val loginViewModel: LoginViewModel by viewModels()

	@Inject
	lateinit var oauthClient: OAuthClient

	@Inject
	lateinit var signUpNavigator: SignUpActivityNavigator

	@Inject
	lateinit var mainNavigator: MainNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initViewState()
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		loginViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		loginViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding) {
			kakaoLoginBtn.setOnClickListener { loginViewModel.onClickKakaoLoginBtn() }
			googleLoginBtn.setOnClickListener { loginViewModel.onClickGoogleLoginBtn() }
		}
	}

	private fun setViewState(uiState: LoginUiState) {
		binding.progressbar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
	}

	private fun startKakaoLogin() = lifecycleScope.launch {
		runCatching { oauthClient.login(this@LoginActivity, OAuth2Provider.KAKAO) }
			.onSuccess { loginViewModel.onChangeIdToken() }
			.onFailure { loginViewModel.onFailedKakaoLogin(it) }
	}

	private fun startGoogleLogin() = lifecycleScope.launch {
		runCatching { oauthClient.login(this@LoginActivity, OAuth2Provider.GOOGLE) }
			.onSuccess { loginViewModel.onChangeIdToken() }
			.onFailure { loginViewModel.onFailedGoogleLogin(it) }
	}

	private fun moveToMain() {
		mainNavigator.navigate(
			currentActivity = this,
			shouldFinish = true,
		)
	}

	private fun moveToSignUp() {
		signUpNavigator.navigate(
			currentActivity = this,
		)
	}

	private fun showDeviceChangeWarning() {
		val existingFragment = supportFragmentManager.findFragmentByTag(DIALOG_TAG_DEVICE_WARNING)
		if (existingFragment != null) return
		val deviceWarningDialog = DeviceWarningDialog(
			onClickOkBtn = { loginViewModel.onClickDeviceWarningOk() },
		)
		deviceWarningDialog.show(supportFragmentManager, DIALOG_TAG_DEVICE_WARNING)
	}

	private fun handleEvent(event: LoginEvent) {
		when (event) {
			is LoginEvent.MoveToMain -> moveToMain()
			is LoginEvent.MoveToSignUp -> moveToSignUp()
			is LoginEvent.ShowDeviceWarning -> showDeviceChangeWarning()
			is LoginEvent.ShowSnackBar -> binding.loginLayout.showSnackBar(event.stringId)
			is LoginEvent.StartKakaoLogin -> startKakaoLogin()
			LoginEvent.StartGoogleLogin -> startGoogleLogin()
		}
	}

	companion object {
		private const val DIALOG_TAG_DEVICE_WARNING = "DIALOG_TAG_DEVICE_WARNING"
	}
}