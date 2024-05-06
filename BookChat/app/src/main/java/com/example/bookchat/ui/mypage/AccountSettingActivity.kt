package com.example.bookchat.ui.mypage

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAccountSettingBinding
import com.example.bookchat.ui.login.LoginActivity
import com.example.bookchat.ui.mypage.AccountSettingViewModel.AccountSettingUiEvent
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
		with(binding) {
			lifecycleOwner = this@AccountSettingActivity
			viewmodel = accountSettingViewModel
		}
		observeUiEvent()
	}

	private fun observeUiEvent() {
		lifecycleScope.launch {
			accountSettingViewModel.eventFlow.collect { event -> handleEvent(event) }
		}
	}

	private fun moveToLoginActivity() {
		val intent = Intent(this, LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK //새로운 태스크 생성
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // 실행 액티비티 외 모두 제거
		startActivity(intent)
	}

	private fun handleEvent(event: AccountSettingUiEvent) {
		when (event) {
			is AccountSettingUiEvent.MoveToLoginPage -> moveToLoginActivity()
			is AccountSettingUiEvent.MakeToast -> makeToast(event.stringId)
		}
	}

}