package com.kova700.bookchat.core.remoteconfig

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.kova700.bookchat.core.remoteconfig.model.RemoteConfigValues
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RemoteConfigManager @Inject constructor() {

	private val remoteConfig by lazy {
		Firebase.remoteConfig.apply {
			setConfigSettingsAsync(
				remoteConfigSettings {
					minimumFetchIntervalInSeconds = 10 //TODO : 적절한 값으로 수정 필요
				}
			)
		}
	}

	suspend fun getRemoteConfig(): RemoteConfigValues {
		return suspendCancellableCoroutine<RemoteConfigValues> { continuation ->
			remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
				when {
					task.isSuccessful -> {
						continuation.resume(
							RemoteConfigValues(
								isServerEnabled = remoteConfig.getBoolean(IS_SERVER_ENABLED_KEY),
								isServerUnderMaintenance = remoteConfig.getBoolean(IS_SERVER_UNDER_MAINTENANCE_KEY),
								serverDownNoticeMessage = remoteConfig.getStringWithMultiLine(
									SERVER_DOWN_NOTICE_TEXT_KEY
								),
								serverUnderMaintenanceNoticeMessage = remoteConfig.getStringWithMultiLine(
									SERVER_UNDER_MAINTENANCE_NOTICE_TEXT_KEY
								),
							).also {
								Log.d("ㄺ", "RemoteConfigManager: getRemoteConfig() - $it")
							}
						)
					}

					else -> continuation.resumeWithException(task.exception ?: Exception("Unknown exception"))
				}
			}
		}
	}

	private fun FirebaseRemoteConfig.getStringWithMultiLine(key: String) =
		getString(key).replace("\\n", "\n")

	companion object {
		private const val IS_SERVER_ENABLED_KEY = "is_server_enabled"
		private const val IS_SERVER_UNDER_MAINTENANCE_KEY = "is_server_under_maintenance"
		private const val SERVER_DOWN_NOTICE_TEXT_KEY = "server_down_notice_text"
		private const val SERVER_UNDER_MAINTENANCE_NOTICE_TEXT_KEY =
			"server_under_maintenance_notice_text"
	}
}