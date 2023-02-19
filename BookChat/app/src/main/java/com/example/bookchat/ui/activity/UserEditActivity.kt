package com.example.bookchat.ui.activity

import android.content.Intent
import android.net.Uri
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
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.viewmodel.UserEditViewModel
import com.example.bookchat.viewmodel.UserEditViewModel.UserEditUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserEditActivity : AppCompatActivity() {
    private lateinit var permissionsLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var binding :ActivityUserEditBinding
    private val userEditViewModel : UserEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_edit)
        permissionsLauncher = PermissionManager.getPermissionsLauncher(this)
        with(binding){
            lifecycleOwner = this@UserEditActivity
            viewmodel = userEditViewModel
            activity = this@UserEditActivity
        }
        setBackPressedDispatcher()
        observeUiEvent()
    }

    fun startUserProfileEdit(){
        permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
        if (PermissionManager.haveGalleryPermission(this)){
            galleryActivityResultLauncher.launch(LAUNCHER_INPUT_IMAGE)
        }
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { resultUri ->
            resultUri?.let { openCropActivity(it) }
        }

    private fun openCropActivity(uri : Uri){
        val intent = Intent(this, ImageCropActivity::class.java)
        intent.putExtra(EXTRA_USER_PROFILE_URI, uri.toString())
        cropActivityResultLauncher.launch(intent)
    }

    private val cropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val intent = result.data
                val bitmapByteArray = intent?.getByteArrayExtra(EXTRA_USER_PROFILE_BYTE_ARRAY1) ?: byteArrayOf()
                userEditViewModel.newProfileImage.value = bitmapByteArray
            }
        }

    private fun observeUiEvent() = lifecycleScope.launch{
        userEditViewModel.eventFlow.collect{ event -> handleUiEvent(event)}
    }

    private fun setBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback {
            userEditViewModel.saveChange()
        }
    }

    private fun finishActivity(){
        finish()
    }

    private fun handleUiEvent(event: UserEditUiEvent) = when(event) {
        is UserEditUiEvent.Finish -> { finishActivity() }
        is UserEditUiEvent.UnknownError -> { makeToast(R.string.error_else) }
    }

    private fun makeToast(stringId :Int){
        Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val LAUNCHER_INPUT_IMAGE = "image/*"
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
    }
}