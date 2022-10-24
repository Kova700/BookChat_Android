package com.example.bookchat.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.SignUpViewModel.SignUpEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var signUpViewModel : SignUpViewModel
    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        signUpViewModel = ViewModelProvider(this, ViewModelFactory()).get(SignUpViewModel::class.java)
        with(binding){
            lifecycleOwner = this@SignUpActivity
            viewModel = signUpViewModel
        }
        setFocus()

        lifecycleScope.launch {
            signUpViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
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

    private fun launchPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        closeKeyboard(binding.nickNameEt.windowToken)
        requestPermissions.launch(permissions)
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result: Map<String, Boolean> ->
        val deniedPermissionList = result.filter { !it.value }.map { it.key }
        if (deniedPermissionList.isNotEmpty()) {
            deniedPermissionHandler(deniedPermissionList)
            return@registerForActivityResult
        }
        openGallery()
    }

    private fun deniedPermissionHandler(
        deniedPermissionList : List<String>
    ){
        val map = deniedPermissionList.groupBy { permission ->
            if (shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED
        }

        map[DENIED]?.let{
            Snackbar.make(binding.signUpLayout,R.string.message_permission_denied, Snackbar.LENGTH_LONG).show()
            return
        }

        map[EXPLAINED]?.let {
            Snackbar.make(binding.signUpLayout,R.string.message_permission_explained, Snackbar.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(SCHEME_PACKAGE, this.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun openGallery(){
        galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
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
        is SignUpEvent.UnknownError -> { Snackbar.make(binding.signUpLayout,R.string.message_error_else,Snackbar.LENGTH_SHORT).show() }
        is SignUpEvent.PermissionCheck -> launchPermissions()
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
        const val DENIED = "DENIED" //처음 거부 : DENIED
        const val EXPLAINED = "EXPLAINED" //두 번째 거부 (다시 묻지 않음) : EXPLAINED
        const val SCHEME_PACKAGE = "package"
        const val LAUNCHER_INPUT_IMAGE = "image/*"
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
        const val EXTRA_SIGNUP_DTO = "EXTRA_SIGNUP_DTO"
        const val EXTRA_USER_PROFILE_BYTE_ARRAY2 = "EXTRA_USER_PROFILE_BYTE_ARRAY2"
    }

}