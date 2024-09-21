package com.kova700.bookchat.feature.mypage.setting.appsetting

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.feature.mypage.databinding.ActivityAppSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppSettingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAppSettingBinding
	private val appSettingViewModel: AppSettingViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAppSettingBinding.inflate(layoutInflater)
		setContentView(binding.root)
		initViewState()
		observeUiState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		appSettingViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun initViewState() {
		binding.backBtn.setOnClickListener { finish() }
		binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
			appSettingViewModel.onChangeNotificationSwitchState(isChecked)
		}
	}

	private fun setViewState(uiState: AppSettingUiState) {
		binding.notificationSwitch.isChecked = uiState.isPushNotificationEnabled
	}

}