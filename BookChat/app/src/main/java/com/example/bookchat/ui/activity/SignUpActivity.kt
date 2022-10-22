package com.example.bookchat.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.viewmodel.LoginViewModel
import com.example.bookchat.viewmodel.SignUpViewModel
import com.example.bookchat.viewmodel.SignUpViewModel.SignUpEvent
import com.example.bookchat.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var signUpViewModel : SignUpViewModel
    private lateinit var imm :InputMethodManager

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
        openKeyboard()
    }

    private fun openKeyboard() {
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Handler(Looper.getMainLooper()).postDelayed({
            imm.showSoftInput(binding.nickNameEt,0)
        },200)
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result: Map<String, Boolean> -> //권한별 결과값 Boolean값으로 가짐

        val deniedList = result.filter { !it.value }.map { it.key }
        when{
            deniedList.isNotEmpty() -> { //명시적 거부 : DENIED / 다시 묻지 않음(두 번 거부) : EXPLAINED
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED }

                map[DENIED]?.let{
                    Snackbar.make(binding.signUpLayout,R.string.message_permission_denied, Snackbar.LENGTH_LONG).show()
                }

                map[EXPLAINED]?.let {
                    Snackbar.make(binding.signUpLayout,R.string.message_permission_explained, Snackbar.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    this.packageName.also { name ->
                        val uri = Uri.fromParts(SCHEME_PACKAGE, name, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                }
            }
            else -> openGallery()
        }
    }

    private fun launchPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestPermissions.launch(permissions) //권한 검사 및 요청
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
            openCropActivity(resultUri!!)
        }

    private fun openGallery(){
        activityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                //크롭된 이미지 가져오기기
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra(EXTRA_USER_PROFILE_BYTE_ARRAY1) ?: byteArrayOf()
                signUpViewModel._userProfilByteArray.value = bitmapByteArray
            }
        }

    private fun openCropActivity(uri :Uri){
        val intent = Intent(this, ImageCropActivity::class.java)
        intent.putExtra(EXTRA_USER_PROFILE_URI,uri.toString())
        cropActivityResultLauncher.launch(intent)
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
        const val DENIED = "DENIED"
        const val EXPLAINED = "EXPLAINED"
        const val SCHEME_PACKAGE = "package"
        const val LAUNCHER_INPUT_IMAGE = "image/*"
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
        const val EXTRA_SIGNUP_DTO = "EXTRA_SIGNUP_DTO"
        const val EXTRA_USER_PROFILE_BYTE_ARRAY2 = "EXTRA_USER_PROFILE_BYTE_ARRAY2"
    }

}