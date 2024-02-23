package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.oauth.GoogleSDK
import com.example.bookchat.ui.dialog.DeviceWarningDialog
import com.example.bookchat.ui.viewmodel.LoginViewModel
import com.example.bookchat.ui.viewmodel.LoginViewModel.LoginEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        with(binding) {
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
            viewmodel = loginViewModel
        }
        observeUiEvent()
    }

    private fun observeUiEvent() = lifecycleScope.launch {
        loginViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    fun clickKakaoLoginBtn() = loginViewModel.clickKakaoLoginBtn(this)
    fun clickGoogleLoginBtn() =
        loginViewModel.clickGoogleLoginBtn(this, googleLoginActivityResultLauncher)

    private val googleLoginActivityResultLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginViewModel.setUiStateToDefault()
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            runCatching {
                GoogleSDK.getSignedInAccountFromIntent(result.data)
                loginViewModel.bookchatLogin()
            }
        }

    private fun showSnackBar(textId: Int) {
        Snackbar.make(binding.loginLayout, textId, Snackbar.LENGTH_SHORT).show()
    }

    private fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun moveToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun showDeviceChangeWarning() {
        val deviceWarningDialog = DeviceWarningDialog()
        deviceWarningDialog.setOkClickListener(
            object : DeviceWarningDialog.OnOkClickListener {
                override fun onOkClick() {
                    loginViewModel.bookchatLogin(true)
                }
            }
        )
        deviceWarningDialog.show(this.supportFragmentManager, DIALOG_TAG_DEVICE_WARNING)
    }

    private fun handleEvent(event: LoginEvent) = when (event) {
        is LoginEvent.MoveToMain -> moveToMain()
        is LoginEvent.MoveToSignUp -> moveToSignUp()
        is LoginEvent.ShowDeviceWarning -> showDeviceChangeWarning()
        is LoginEvent.ForbiddenUser -> {
            showSnackBar(R.string.login_forbidden_user)
        }

        is LoginEvent.NetworkError -> {
            showSnackBar(R.string.error_network)
        }

        is LoginEvent.KakaoLoginFail -> {
            showSnackBar(R.string.error_kakao_login)
        }

        is LoginEvent.UnknownError -> {
            showSnackBar(R.string.error_else)
        }
    }

    companion object {
        private const val DIALOG_TAG_DEVICE_WARNING = "DIALOG_TAG_DEVICE_WARNING"
    }
}