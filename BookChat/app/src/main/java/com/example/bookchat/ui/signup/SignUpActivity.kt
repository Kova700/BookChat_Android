package com.example.bookchat.ui.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.imagecrop.ImageCropActivity.Companion.EXTRA_CROPPED_PROFILE_BYTE_ARRAY
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.ui.signup.SignUpViewModel.SignUpEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        permissionsLauncher = PermissionManager.getPermissionsLauncher(this) { openCropActivity() }
        with(binding) {
            lifecycleOwner = this@SignUpActivity
            viewModel = signUpViewModel
        }
        setFocus()
        observeEvent()
    }

    private fun observeEvent() = lifecycleScope.launch {
        signUpViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun setFocus() {
        binding.nickNameEt.requestFocus()
        openKeyboard(binding.nickNameEt)
    }

    private fun openKeyboard(view: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }, KEYBOARD_DELAY_TIME)
    }

    private fun closeKeyboard(windowToken: IBinder) {
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun startUserProfileEdit() {
        closeKeyboard(binding.nickNameEt.windowToken)
        permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
    }

    private fun openCropActivity() {
        val intent = Intent(this, ImageCropActivity::class.java)
        cropActivityResultLauncher.launch(intent)
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                val bitmapByteArray =
                    intent?.getByteArrayExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY) ?: byteArrayOf()
                signUpViewModel.userProfileByteArray.value = bitmapByteArray
            }
        }

    private fun moveToSelectTaste(event: SignUpEvent.MoveToSelectTaste) {
        val intent = Intent(this, SelectTasteActivity::class.java)
        intent.putExtra(EXTRA_SIGNUP_USER_NICKNAME, event.userNickname)
        intent.putExtra(EXTRA_USER_PROFILE_BYTE_ARRAY2, event.userProfilByteArray)
        startActivity(intent)
    }

    private fun showSnackBar(messageId :Int){
        Snackbar.make(binding.signUpLayout,messageId, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleEvent(event: SignUpEvent) = when (event) {
        is SignUpEvent.UnknownError -> showSnackBar(R.string.error_else)
        is SignUpEvent.PermissionCheck -> startUserProfileEdit()
        is SignUpEvent.MoveToBack -> finish()
        is SignUpEvent.MoveToSelectTaste -> moveToSelectTaste(event)
    }

    companion object {
        const val KEYBOARD_DELAY_TIME = 200L
        const val EXTRA_SIGNUP_USER_NICKNAME = "EXTRA_SIGNUP_USER_NICKNAME"
        const val EXTRA_USER_PROFILE_BYTE_ARRAY2 = "EXTRA_USER_PROFILE_BYTE_ARRAY2"
    }
}