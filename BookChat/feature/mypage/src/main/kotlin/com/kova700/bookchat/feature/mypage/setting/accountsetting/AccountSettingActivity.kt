package com.kova700.bookchat.feature.mypage.setting.accountsetting

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.navigation.LoginActivityNavigator
import com.kova700.bookchat.core.oauth.external.OAuthClient
import com.kova700.bookchat.feature.mypage.databinding.ActivityAccountSettingBinding
import com.kova700.bookchat.feature.mypage.setting.accountsetting.dialog.WithdrawWarningDialog
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountSettingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAccountSettingBinding
	private val accountSettingViewModel: AccountSettingViewModel by viewModels()

	@Inject
	lateinit var oauthClient: OAuthClient

	@Inject
	lateinit var loginNavigator: LoginActivityNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAccountSettingBinding.inflate(layoutInflater)
		setBackPressedDispatcher()
		setContentView(binding.root)
		initViewState()
		observeUiState()
		observeUiEvent()
	}

	private fun observeUiState() = lifecycleScope.launch {
		accountSettingViewModel.uiState.collect { state -> setViewState(state) }
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		accountSettingViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(state: AccountSettingUiState) {
		binding.progressBar.visibility = if (state.isLoading) VISIBLE else GONE
	}

	private fun initViewState() {
		with(binding) {
			backBtn.setOnClickListener { accountSettingViewModel.onClickBackBtn() }
			logoutBtn.setOnClickListener { accountSettingViewModel.onClickLogoutBtn() }
			withdrawBtn.setOnClickListener { accountSettingViewModel.onClickWithdrawBtn() }
		}
	}

	private fun startOAuthLogout() = lifecycleScope.launch {
		runCatching { oauthClient.logout(this@AccountSettingActivity) }
			.onSuccess { accountSettingViewModel.onSuccessOAuthLogout() }
	}

	private fun startOAuthWithdraw() = lifecycleScope.launch {
		runCatching { oauthClient.withdraw(this@AccountSettingActivity) }
			.onSuccess { accountSettingViewModel.onSuccessOAuthWithdraw() }
	}

	private fun moveToLoginActivity() {
		loginNavigator.navigate(
			currentActivity = this,
			intentAction = {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
			},
			shouldFinish = true,
		)
	}

	private fun showWithdrawWarningDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_WITHDRAW_WARNING)
		if (existingFragment != null) return

		val dialog = WithdrawWarningDialog(
			onClickOkBtn = { accountSettingViewModel.onClickWithdrawConfirm() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_WITHDRAW_WARNING)
	}

	private fun handleEvent(event: AccountSettingUiEvent) {
		when (event) {
			is AccountSettingUiEvent.MoveToLoginPage -> moveToLoginActivity()
			is AccountSettingUiEvent.ShowSnackBar -> binding.root.showSnackBar(textId = event.stringId)
			AccountSettingUiEvent.ShowWithdrawWarningDialog -> showWithdrawWarningDialog()
			AccountSettingUiEvent.StartOAuthLogout -> startOAuthLogout()
			AccountSettingUiEvent.StartOAuthWithdraw -> startOAuthWithdraw()
			AccountSettingUiEvent.MoveToBack -> finish()
		}
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			accountSettingViewModel.onClickBackBtn()
		}
	}

	companion object {
		private const val DIALOG_TAG_WITHDRAW_WARNING = "DIALOG_TAG_WITHDRAW_WARNING"
	}
}