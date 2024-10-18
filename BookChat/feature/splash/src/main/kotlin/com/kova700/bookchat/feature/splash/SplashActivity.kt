package com.kova700.bookchat.feature.splash

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.LoginActivityNavigator
import com.kova700.bookchat.core.navigation.MainNavigator
import com.kova700.bookchat.core.remoteconfig.dialog.ServerDownNoticeDialog
import com.kova700.bookchat.core.remoteconfig.dialog.ServerUnderMaintenanceNoticeDialog
import com.kova700.bookchat.feature.splash.dialog.NetworkDisconnectedNoticeDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kova700.bookchat.feature.splash.R as splashR

//TODO : [FixWaiting] SplashScreen 적용
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
	private val splashViewModel: SplashViewModel by viewModels()

	@Inject
	lateinit var loginNavigator: LoginActivityNavigator

	@Inject
	lateinit var mainNavigator: MainNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(splashR.layout.activity_splash)
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		splashViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		splashViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(state: SplashUiState) {
		if (state.isNetworkConnected.not()) showNetworkDisconnectedDialog()
		else dismissNetworkDisconnectedDialog()
	}

	private fun moveToMain() {
		mainNavigator.navigate(
			currentActivity = this,
			shouldFinish = true,
		)
	}

	private fun moveToLogin() {
		loginNavigator.navigate(
			currentActivity = this,
			shouldFinish = true,
		)
	}

	private fun showServerDisabledDialog(noticeMessage: String?) {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(ServerDownNoticeDialog.TAG)
		if (existingFragment != null) return
		val dialog = ServerDownNoticeDialog(
			onClickOkBtn = { finish() },
			noticeMessage = noticeMessage.takeUnless { it.isNullOrBlank() }
				?: getString(R.string.splash_server_down),
		)
		dialog.show(supportFragmentManager, ServerDownNoticeDialog.TAG)
	}

	private fun showServerMaintenanceDialog(noticeMessage: String?) {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(ServerUnderMaintenanceNoticeDialog.TAG)
		if (existingFragment != null) return
		val dialog = ServerUnderMaintenanceNoticeDialog(
			onClickOkBtn = { finish() },
			noticeMessage = noticeMessage.takeUnless { it.isNullOrBlank() }
				?: getString(R.string.splash_server_under_maintenance),
		)
		dialog.show(supportFragmentManager, ServerUnderMaintenanceNoticeDialog.TAG)
	}

	private fun showNetworkDisconnectedDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(NetworkDisconnectedNoticeDialog.TAG)
		if (existingFragment != null) return
		val dialog = NetworkDisconnectedNoticeDialog()
		dialog.show(supportFragmentManager, NetworkDisconnectedNoticeDialog.TAG)
	}

	private fun dismissNetworkDisconnectedDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(NetworkDisconnectedNoticeDialog.TAG) ?: return
		(existingFragment as NetworkDisconnectedNoticeDialog).dismiss()
	}

	private fun handleEvent(event: SplashEvent) = when (event) {
		is SplashEvent.MoveToMain -> moveToMain()
		is SplashEvent.MoveToLogin -> moveToLogin()
		is SplashEvent.ShowServerDisabledDialog -> showServerDisabledDialog(event.message)
		is SplashEvent.ShowServerMaintenanceDialog -> showServerMaintenanceDialog(event.message)
	}
}