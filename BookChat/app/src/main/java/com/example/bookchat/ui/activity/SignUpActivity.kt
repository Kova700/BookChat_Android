package com.example.bookchat.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
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
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.SignUpViewModel.SignUpEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private val signUpViewModel : SignUpViewModel by viewModels()
    private lateinit var permissionsLauncher : ActivityResultLauncher<Array<String>>
    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        permissionsLauncher = PermissionManager.getPermissionsLauncher(this)
        with(binding){
            lifecycleOwner = this@SignUpActivity
            viewModel = signUpViewModel
        }
        setFocus()
        observeEvent()
    }

    private fun observeEvent() = lifecycleScope.launch {
        signUpViewModel.eventFlow.collect { event -> handleEvent(event) }
    }

    private fun setFocus(){
        binding.nickNameEt.requestFocus()
        openKeyboard(binding.nickNameEt)
    }

    private fun openKeyboard(view :View) {
        Handler(Looper.getMainLooper()).postDelayed({
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT)
        },DELAY_TIME)
    }

    private fun closeKeyboard(windowToken :IBinder) {
        imm.hideSoftInputFromWindow(windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun startUserProfileEdit() {
        closeKeyboard(binding.nickNameEt.windowToken)
        permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
        if (PermissionManager.haveGalleryPermission(this)){
            galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
        }
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
            resultUri?.let { openCropActivity(it) }
        }

    private fun openCropActivity(uri :Uri){
        val intent = Intent(this, ImageCropActivity::class.java)
        intent.putExtra(EXTRA_USER_PROFILE_URI,uri.toString())
        cropActivityResultLauncher.launch(intent)
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra(EXTRA_USER_PROFILE_BYTE_ARRAY1) ?: byteArrayOf()
                signUpViewModel._userProfilByteArray.value = bitmapByteArray
            }
        }

    private fun handleEvent(event: SignUpEvent) = when(event) {
        is SignUpEvent.UnknownError -> { Snackbar.make(binding.signUpLayout,R.string.error_else,Snackbar.LENGTH_SHORT).show() }
        is SignUpEvent.PermissionCheck -> startUserProfileEdit()
        is SignUpEvent.MoveToBack -> finish()
        is SignUpEvent.MoveToSelectTaste -> {
            val intent = Intent(this, SelectTasteActivity::class.java)
            intent.putExtra(EXTRA_SIGNUP_DTO , event.signUpDto)
            intent.putExtra(EXTRA_USER_PROFILE_BYTE_ARRAY2,event.userProfilByteArray)
            startActivity(intent)
        }
    }

    companion object{
        const val DELAY_TIME = 200L
        const val LAUNCHER_INPUT_IMAGE = "image/*"
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
        const val EXTRA_SIGNUP_DTO = "EXTRA_SIGNUP_DTO"
        const val EXTRA_USER_PROFILE_BYTE_ARRAY2 = "EXTRA_USER_PROFILE_BYTE_ARRAY2"
    }
}