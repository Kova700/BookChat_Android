package com.kova700.bookchat.util.permissions

import android.Manifest
import android.os.Build

val galleryPermissions =
	if (isAndroidVersionAtLeastTiramisu()) {
		arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
	} else arrayOf(
		Manifest.permission.READ_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_EXTERNAL_STORAGE
	)

val notificationPermission =
	if (isAndroidVersionAtLeastTiramisu()) {
		arrayOf(Manifest.permission.POST_NOTIFICATIONS)
	} else emptyArray()

private fun isAndroidVersionAtLeastTiramisu(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}
