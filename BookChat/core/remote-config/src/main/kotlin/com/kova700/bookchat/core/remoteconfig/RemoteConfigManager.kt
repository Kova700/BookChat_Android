package com.kova700.bookchat.core.remoteconfig

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
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
					minimumFetchIntervalInSeconds = 3600
				}
			)
		}
	}

	init {
		observeRemoteConfigUpdate()
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
							)
						)
					}

					else -> continuation.resumeWithException(task.exception ?: Exception("Unknown exception"))
				}
			}
		}
	}

	private fun observeRemoteConfigUpdate() {
		remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
			override fun onUpdate(configUpdate: ConfigUpdate) {
				val updatedKeys = configUpdate.updatedKeys
				if (updatedKeys.contains(IS_SERVER_ENABLED_KEY)
					|| updatedKeys.contains(IS_SERVER_UNDER_MAINTENANCE_KEY)
					|| updatedKeys.contains(SERVER_DOWN_NOTICE_TEXT_KEY)
					|| updatedKeys.contains(SERVER_UNDER_MAINTENANCE_NOTICE_TEXT_KEY)
				) {
					remoteConfig.activate()
				}
			}

			override fun onError(error: FirebaseRemoteConfigException) {}
		})
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