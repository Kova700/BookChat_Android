package com.example.bookchat.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.oauth.google.external.GoogleLoginClient
import com.example.bookchat.oauth.google.external.exception.GoogleLoginClientCancelException
import com.example.bookchat.oauth.kakao.external.KakaoLoginClient
import com.example.bookchat.oauth.kakao.external.exception.KakaoLoginUserCancelException
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.signup.SignUpActivity
import com.example.bookchat.utils.makeToast
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

	private fun setViewState(uiState: LoginUiState) {
		binding.progressbar.visibility =
			if (uiState.uiState == LoginUiState.UiState.LOADING) View.VISIBLE else View.GONE
	}

	//TODO : 그냥 단순 유저 Cancel이면 로그인 실패 안띄우게 수정
	private fun startKakaoLogin() = lifecycleScope.launch {
		runCatching { kakaoLoginClient.login(this@LoginActivity) }
			.onSuccess {
				loginViewModel.onChangeIdToken(it)
				loginViewModel.login()
			}
			.onFailure {
				if (it is KakaoLoginUserCancelException) return@onFailure
				makeToast(R.string.error_kakao_login)
			}
	}

	private fun startGoogleLogin() = lifecycleScope.launch {
		runCatching { googleLoginClient.login(this@LoginActivity) }
			.onSuccess {
				loginViewModel.onChangeIdToken(it)
				loginViewModel.login()
			}
			.onFailure {
				if (it is GoogleLoginClientCancelException) return@onFailure
				makeToast(R.string.error_google_login)
			}
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