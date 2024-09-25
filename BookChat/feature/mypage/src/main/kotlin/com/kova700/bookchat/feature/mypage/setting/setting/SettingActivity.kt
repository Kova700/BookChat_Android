package com.kova700.bookchat.feature.mypage.setting.setting

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.feature.mypage.databinding.ActivitySettingBinding
import com.kova700.bookchat.feature.mypage.setting.accountsetting.AccountSettingActivity
import com.kova700.bookchat.feature.mypage.setting.appsetting.AppSettingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySettingBinding
	private val settingViewModel: SettingViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initViewState()
		observeUiEvent()
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		settingViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { finish() }
		binding.accountSettingBtn.setOnClickListener { settingViewModel.onClickAccountSettingBtn() }
		binding.appSettingBtn.setOnClickListener { settingViewModel.onClickAppSettingBtn() }
	}

	private fun moveToAccountSetting() {
		val intent = Intent(this, AccountSettingActivity::class.java)
		startActivity(intent)
	}

	private fun moveToAppSetting() {
		val intent = Intent(this, AppSettingActivity::class.java)
		startActivity(intent)
	}

	private fun handleEvent(event: SettingUiEvent) {
		when (event) {
			SettingUiEvent.MoveToAccountSetting -> moveToAccountSetting()
			SettingUiEvent.MoveToAppSetting -> moveToAppSetting()
		}
	}
}