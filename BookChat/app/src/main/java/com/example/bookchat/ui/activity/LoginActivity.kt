package com.example.bookchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.example.bookchat.databinding.ActivityLoginBinding
import com.example.bookchat.oauth.GoogleSDK
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.utils.OAuth2Provider.GOOGLE
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.LoginViewModel.LoginEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private val loginViewModel : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        with(binding){
            lifecycleOwner = this@LoginActivity
            activity = this@LoginActivity
        }

        observeUiEvent()
    }

    private fun observeUiEvent() = lifecycleScope.launch {
        loginViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    fun clickKakaoLoginBtn(){
        if (loginViewModel.isUiStateDefault()){
            loginViewModel.setUiStateToLoading()
            loginViewModel.startKakaoLogin(this)
        }
    }

    fun clickGoogleLoginBtn(){
        if (loginViewModel.isUiStateDefault()){
            startGoogleLoginActivity()
        }
    }

    private fun startGoogleLoginActivity(){
        loginViewModel.setUiStateToLoading()
        val signInIntent = GoogleSDK.getSignInIntent(this)
        googleLoginActivityResultLauncher.launch(signInIntent)
    }

    private val googleLoginActivityResultLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginViewModel.setUiStateToDefault()

            if(result.resultCode == RESULT_OK){
                runCatching {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    saveGoogleIdToken(task.result.idToken)
                    loginViewModel.bookchatLogin()
                }
            }
        }

    private fun saveGoogleIdToken(idToken :String?) {
        idToken ?: throw TokenDoseNotExistException()
        DataStoreManager.saveIdToken(IdToken("Bearer $idToken", GOOGLE))
    }

    private fun showSnackBar(textId :Int){
        Snackbar.make(binding.loginLayout, textId, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleEvent(event: LoginEvent) = when(event) {
        is LoginEvent.MoveToMain -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
        is LoginEvent.MoveToSignUp -> { startActivity(Intent(this, SignUpActivity::class.java)); }
        is LoginEvent.ForbiddenUser -> { showSnackBar(R.string.login_forbidden_user) }
        is LoginEvent.NetworkError -> { showSnackBar(R.string.error_network) }
        is LoginEvent.KakaoLoginFail -> { showSnackBar(R.string.error_kakao_login) }
        is LoginEvent.UnknownError -> { showSnackBar(R.string.error_else)}
        is LoginEvent.NeedToGoogleLogin -> { startGoogleLoginActivity() }
    }
}