package com.kova700.bookchat.core.fcm.forced_logout

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kova700.bookchat.core.fcm.forced_logout.dialog.DeviceChangeForcedLogoutNoticeDialog
import com.kova700.bookchat.core.fcm.forced_logout.dialog.TokenExpiredForcedLogoutNoticeDialog
import com.kova700.bookchat.feature.login.LoginActivity
import com.kova700.core.domain.usecase.client.LogoutUseCase
import javax.inject.Inject

//TODO : [FixWaiting] 같은 기기로 재로그인 시에, 같은 기기가 로그아웃 FCM을 받는현상 발생
class ForcedLogoutManagerImpl @Inject constructor(
	private val logoutUseCase: LogoutUseCase,
) : ForcedLogoutManager {
	private var currentActivity: Activity? = null
	private var shouldShowLogoutNoticeDialog = false

	override fun start(application: Application) {
		application.registerActivityLifecycleCallbacks(this)
	}

	override suspend fun onDeviceChanged() {
		shouldShowLogoutNoticeDialog = true
		logoutUseCase(needServer = false)
		showDeviceChangedLogoutDialog()
	}

	override suspend fun onBookChatTokenExpired() {
		shouldShowLogoutNoticeDialog = true
		logoutUseCase(needServer = false)
		showTokenExpiredDialog()
	}

	override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
		currentActivity = activity
	}

	override fun onActivityStarted(activity: Activity) {
		currentActivity = activity
		showDeviceChangedLogoutDialog()
	}

	override fun onActivityResumed(activity: Activity) {
		currentActivity = activity
		showDeviceChangedLogoutDialog()
	}

	override fun onActivityPaused(activity: Activity) {}
	override fun onActivityStopped(activity: Activity) {
		if (currentActivity == activity) currentActivity = null
	}

	override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
	override fun onActivityDestroyed(activity: Activity) {}

	private fun showTokenExpiredDialog() {
		if (shouldShowLogoutNoticeDialog.not()) return

		val supportFragmentManager =
			(currentActivity as? AppCompatActivity ?: return).supportFragmentManager

		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_TOKEN_EXPIRED_LOGOUT_NOTICE)
		if (existingFragment != null) return

		val dialog = TokenExpiredForcedLogoutNoticeDialog(
			onClickOkBtn = {
				moveToLoginActivity()
				shouldShowLogoutNoticeDialog = false
			}
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_TOKEN_EXPIRED_LOGOUT_NOTICE)
	}

	private fun showDeviceChangedLogoutDialog() {
		if (shouldShowLogoutNoticeDialog.not()) return

		val supportFragmentManager =
			(currentActivity as? AppCompatActivity ?: return).supportFragmentManager

		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_DEVICE_CHANGE_LOGOUT_NOTICE)
		if (existingFragment != null) return

		val dialog = DeviceChangeForcedLogoutNoticeDialog(
			onClickOkBtn = {
				moveToLoginActivity()
				shouldShowLogoutNoticeDialog = false
			}
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_DEVICE_CHANGE_LOGOUT_NOTICE)
	}

	private fun moveToLoginActivity() {
		if (currentActivity == null || currentActivity is LoginActivity) return
		val intent = Intent(currentActivity, LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		currentActivity?.startActivity(intent)
		currentActivity?.finish()
	}

	companion object {
		private const val DIALOG_TAG_DEVICE_CHANGE_LOGOUT_NOTICE =
			"DIALOG_TAG_DEVICE_CHANGE_LOGOUT_NOTICE"
		private const val DIALOG_TAG_TOKEN_EXPIRED_LOGOUT_NOTICE =
			"DIALOG_TAG_TOKEN_EXPIRED_LOGOUT_NOTICE"
	}
}