package com.example.bookchat.ui.mypage.accountsetting

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAccountSettingBinding
import com.example.bookchat.ui.login.LoginActivity
import com.example.bookchat.ui.mypage.accountsetting.dialog.WithdrawWarningDialog
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountSettingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAccountSettingBinding
	private val accountSettingViewModel: AccountSettingViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_account_setting)
		binding.lifecycleOwner = this
		initViewState()
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		accountSettingViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { finish() }
		binding.logoutBtn.setOnClickListener { accountSettingViewModel.onClickLogoutBtn() }
		binding.withdrawBtn.setOnClickListener { accountSettingViewModel.onClickWithdrawBtn() }
	}

	private fun moveToLoginActivity() {
		val intent = Intent(this, LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		startActivity(intent)
		finish()
	}

	//TODO : 방장이 회원탈퇴시 채팅방 터지는지 테스트 필요
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
			is AccountSettingUiEvent.MakeToast -> makeToast(event.stringId)
			AccountSettingUiEvent.ShowWithdrawWarningDialog -> showWithdrawWarningDialog()
		}
	}

	companion object {
		private const val DIALOG_TAG_WITHDRAW_WARNING = "DIALOG_TAG_WITHDRAW_WARNING"
	}
}