package com.example.bookchat.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.ui.activity.ImageCropActivity.Companion.EXTRA_USER_PROFILE_BYTE_ARRAY1
import com.example.bookchat.databinding.ActivityUserEditBinding
import com.example.bookchat.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class UserEditActivity : AppCompatActivity() {
    private lateinit var permissionsLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var binding :ActivityUserEditBinding
    val userImage = MutableStateFlow(byteArrayOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_edit)
        permissionsLauncher = PermissionManager.getPermissionsLauncher(this)
        with(binding){
            lifecycleOwner = this@UserEditActivity
            activity = this@UserEditActivity
            user = App.instance.getCachedUser()
        }
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
                userImage.value = bitmapByteArray
            }
        }

    fun clickBackBtn(){
        finish()
    }

    companion object{
        const val LAUNCHER_INPUT_IMAGE = "image/*"
        const val EXTRA_USER_PROFILE_URI = "EXTRA_USER_PROFILE_URI"
    }
}