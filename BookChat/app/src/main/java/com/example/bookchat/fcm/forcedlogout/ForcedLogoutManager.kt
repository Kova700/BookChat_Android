package com.example.bookchat.fcm.forcedlogout

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bookchat.domain.usecase.LogoutUseCase
import com.example.bookchat.ui.login.LoginActivity
import javax.inject.Inject

interface ForcedLogoutManager : Application.ActivityLifecycleCallbacks {
	fun start(application: Application)
	suspend fun onLogoutMessageReceived()
}

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
		logoutUseCase()
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