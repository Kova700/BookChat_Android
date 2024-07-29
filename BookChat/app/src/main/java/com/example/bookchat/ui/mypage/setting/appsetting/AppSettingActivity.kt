package com.example.bookchat.ui.mypage.setting.appsetting

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityAppSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppSettingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityAppSettingBinding
	private val appSettingViewModel: AppSettingViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_app_setting)
		binding.lifecycleOwner = this
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