package com.example.bookchat.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityUserEditBinding
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.imagecrop.ImageCropActivity.Companion.EXTRA_CROPPED_PROFILE_BYTE_ARRAY
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.ui.mypage.UserEditViewModel.UserEditUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserEditActivity : AppCompatActivity() {
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityUserEditBinding
    private val userEditViewModel: UserEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_edit)
        permissionsLauncher = PermissionManager.getPermissionsLauncher(this) { openCropActivity() }

        with(binding) {
            lifecycleOwner = this@UserEditActivity
            viewmodel = userEditViewModel
            activity = this@UserEditActivity
        }
        setBackPressedDispatcher()
        observeUiEvent()
    }

    fun startUserProfileEdit() {
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
                userEditViewModel.newProfileImage.value = bitmapByteArray
            }
        }

    private fun observeUiEvent() = lifecycleScope.launch {
        userEditViewModel.eventFlow.collect { event -> handleUiEvent(event) }
    }

    private fun setBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback {
            userEditViewModel.saveChange()
        }
    }

    private fun finishActivity() {
        finish()
    }

    private fun handleUiEvent(event: UserEditUiEvent) = when (event) {
        is UserEditUiEvent.Finish -> {
            finishActivity()
        }
        else -> {
            makeToast(R.string.error_else)
        }
    }

    private fun makeToast(stringId: Int) {
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }
}