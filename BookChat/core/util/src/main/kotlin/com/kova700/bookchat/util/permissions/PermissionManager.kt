package com.kova700.bookchat.util.permissions

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

inline fun AppCompatActivity.getPermissionsLauncher(
	crossinline onSuccess: () -> Unit,
	crossinline onDenied: () -> Unit,
	crossinline onExplained: () -> Unit,
) = registerForActivityResult(
	ActivityResultContracts.RequestMultiplePermissions()
) { result: Map<String, Boolean> ->
	val deniedPermissionList = result.filter { it.value.not() }.map { it.key }
	if (deniedPermissionList.isNotEmpty()) {
		deniedPermissionHandler(deniedPermissionList, onDenied, onExplained)
		return@registerForActivityResult
	}
	onSuccess()
}

inline fun AppCompatActivity.deniedPermissionHandler(
	deniedPermissionList: List<String>,
	onDenied: () -> Unit,
	onExplained: () -> Unit,
) {
	val resultMap = deniedPermissionList.groupBy { permission ->
		if (shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED
	}

	when {
		resultMap[DENIED] != null -> onDenied()
		resultMap[EXPLAINED] != null -> onExplained()
	}
}

const val DENIED = "DENIED"
const val EXPLAINED = "EXPLAINED"