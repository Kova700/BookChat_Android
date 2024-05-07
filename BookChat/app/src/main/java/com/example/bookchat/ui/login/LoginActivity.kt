package com.example.bookchat.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.oauth.google.GoogleLoginClient
import com.example.bookchat.oauth.kakao.KakaoLoginClient
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.signup.SignUpActivity
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
	private lateinit var binding: ActivityLoginBinding
	private val loginViewModel: LoginViewModel by viewModels()

	@Inject
	lateinit var kakaoLoginClient: KakaoLoginClient

	@Inject
	lateinit var googleLoginClient: GoogleLoginClient

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
		binding.lifecycleOwner = this
		binding.viewmodel = loginViewModel
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		loginViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun startKakaoLogin() = lifecycleScope.launch {
		runCatching { kakaoLoginClient.login(this@LoginActivity) }
			.onSuccess {
				loginViewModel.onChangeIdToken(it)
				loginViewModel.login()
			}
			.onFailure { handleEvent(LoginEvent.UnknownErrorEvent("Kakao 로그인을 실패했습니다.")) }
	}

	private fun startGoogleLogin() = lifecycleScope.launch {
		googleLoginClient.login(this@LoginActivity, googleLoginResultLauncher)
	}

	private val googleLoginResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode != RESULT_OK) {
				handleEvent(LoginEvent.UnknownErrorEvent("Google 로그인을 실패했습니다."))
				return@registerForActivityResult
			}
			val idToken = googleLoginClient.getIdTokenFromResultIntent(result.data)
			loginViewModel.onChangeIdToken(idToken)
			loginViewModel.login()
		}

	private fun moveToMain() {
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	private fun moveToSignUp() {
		startActivity(Intent(this, SignUpActivity::class.java))
		finish()
	}

	private fun showDeviceChangeWarning() {
		val deviceWarningDialog = DeviceWarningDialog()
		deviceWarningDialog.show(supportFragmentManager, DIALOG_TAG_DEVICE_WARNING)
	}

	private fun handleEvent(event: LoginEvent) {
		when (event) {
			is LoginEvent.MoveToMain -> moveToMain()
			is LoginEvent.MoveToSignUp -> moveToSignUp()
			is LoginEvent.ShowDeviceWarning -> showDeviceChangeWarning()
			is LoginEvent.ErrorEvent -> binding.loginLayout.showSnackBar(event.stringId)
			is LoginEvent.UnknownErrorEvent -> binding.loginLayout.showSnackBar(event.message)
			is LoginEvent.StartKakaoLogin -> startKakaoLogin()
			LoginEvent.StartGoogleLogin -> startGoogleLogin()
		}
	}

	companion object {
		private const val DIALOG_TAG_DEVICE_WARNING = "DIALOG_TAG_DEVICE_WARNING"
	}
}