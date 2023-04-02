package com.example.bookchat.utils

import android.Manifest.permission
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchat.R

object PermissionManager {

    fun getGalleryPermissions(): Array<String> {
        if (sdkVersionIsMoreTIRAMISU()) {
            return arrayOf(permission.READ_MEDIA_IMAGES)
        }
        return arrayOf(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun getPermissionsLauncher(
        activity: AppCompatActivity,
        successCallBack: () -> Unit
    ) = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val deniedPermissionList = result.filter { !it.value }.map { it.key }
        if (deniedPermissionList.isNotEmpty()) {
            deniedPermissionHandler(activity, deniedPermissionList)
            return@registerForActivityResult
        }
        successCallBack()
    }

    private fun deniedPermissionHandler(
        activity: AppCompatActivity,
        deniedPermissionList: List<String>
    ) {
        val resultMap = deniedPermissionList.groupBy { permission ->
            if (activity.shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED
        }

        resultMap[DENIED]?.let {
            activity.makeToast(R.string.permission_denied)
            return
        }

        resultMap[EXPLAINED]?.let {
            activity.makeToast(R.string.permission_explained)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(SCHEME_PACKAGE, activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
    }

    private fun Activity.makeToast(stringId: Int) =
        Toast.makeText(this, stringId, Toast.LENGTH_LONG).show()

    private fun sdkVersionIsMoreTIRAMISU(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    private const val DENIED = "DENIED"
    private const val EXPLAINED = "EXPLAINED"
    private const val SCHEME_PACKAGE = "package"
}