package com.kova700.bookchat.core.fcm.forced_logout

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kova700.bookchat.core.fcm.forced_logout.dialog.ForcedLogoutNoticeDialog
import com.kova700.bookchat.feature.login.LoginActivity
import com.kova700.core.domain.usecase.client.LogoutUseCase
import javax.inject.Inject

class ForcedLogoutManagerImpl @Inject constructor(
	private val logoutUseCase: LogoutUseCase,
) : ForcedLogoutManager {
	private var currentActivity: Activity? = null
	private var shouldShowLogoutNoticeDialog = false

	override fun start(application: Application) {
		application.registerActivityLifecycleCallbacks(this)
	}

	override suspend fun onLogoutMessageReceived() {
		shouldShowLogoutNoticeDialog = true
		logoutUseCase(needServer = false)
		showLogoutDialog()
	}

	override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
		currentActivity = activity
	}

	override fun onActivityStarted(activity: Activity) {
		currentActivity = activity
		showLogoutDialog()
	}

	override fun onActivityResumed(activity: Activity) {
		currentActivity = activity
		showLogoutDialog()
	}

	override fun onActivityPaused(activity: Activity) {}
	override fun onActivityStopped(activity: Activity) {
		if (currentActivity == activity) currentActivity = null
	}

	override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
	override fun onActivityDestroyed(activity: Activity) {}

	private fun showLogoutDialog() {
		if (shouldShowLogoutNoticeDialog.not()) return

		val supportFragmentManager =
			(currentActivity as? AppCompatActivity ?: return).supportFragmentManager

		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_LOGOUT_NOTICE)
		if (existingFragment != null) return

		val dialog = ForcedLogoutNoticeDialog(
			onClickOkBtn = {
				moveToLoginActivity()
				shouldShowLogoutNoticeDialog = false
			}
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_LOGOUT_NOTICE)
	}

	private fun moveToLoginActivity() {
		if (currentActivity == null || currentActivity is LoginActivity) return
		val intent = Intent(currentActivity, LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		currentActivity?.startActivity(intent)
		currentActivity?.finish()
	}

	companion object {
		private const val DIALOG_TAG_LOGOUT_NOTICE = "DIALOG_TAG_LOGOUT_NOTICE"
	}
}