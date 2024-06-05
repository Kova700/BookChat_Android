package com.example.bookchat.ui.channel.channelsetting

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelSettingBinding
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.host.HostManageActivity
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.SubHostManageActivity
import com.example.bookchat.ui.channel.channelsetting.dialog.ChannelCapacitySettingDialog
import com.example.bookchat.ui.channel.drawer.dialog.ChannelExitWarningDialog
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.utils.MakeChannelImgSizeManager
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelSettingActivity : AppCompatActivity() {
	private lateinit var binding: ActivityChannelSettingBinding

	private val channelSettingViewModel by viewModels<ChannelSettingViewModel>()

	private val permissionsLauncher =
		PermissionManager.getPermissionsLauncher(this) { moveToImageCrop() }

	@Inject
	lateinit var makeChannelImgSizeManager: MakeChannelImgSizeManager
	//TODO : 기본이미지 채널 기본이미지 맞는지 확인

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_channel_setting)
		binding.lifecycleOwner = this
		binding.viewmodel = channelSettingViewModel
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelSettingViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		channelSettingViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding.channelTitleEt) {
			addTextChangedListener { text ->
				val message = text?.toString() ?: return@addTextChangedListener
				channelSettingViewModel.onChangeChannelTitle(message)
			}
		}
		with(binding.channelTagEt) {
			addTextChangedListener { text ->
				val message = text?.toString() ?: return@addTextChangedListener
				channelSettingViewModel.onChangeChannelTags(message)
			}
		}
		makeChannelImgSizeManager.setMakeChannelImgSize(binding.channelImgIvIv)
	}

	private fun setViewState(state: ChannelSettingUiState) {
		with(binding.channelTitleEt) {
			if (state.newTitle != text.toString()) {
				setText(state.newTitle)
				setSelection(state.newTitle.length)
			}
		}
		with(binding.channelTagEt) {
			if (state.newTags != text.toString()) {
				setText(state.newTags)
				setSelection(state.newTags.length)
			}
		}

		with(binding.applyChannelChange) {
			if (state.isPossibleChangeChannel) {
				setTextColor(Color.parseColor("#000000"))
				isEnabled = true
			} else {
				setTextColor(Color.parseColor("#D9D9D9"))
				isEnabled = false
			}
		}
	}

	private fun startChannelProfileEdit() {
		permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
	}

	private fun moveToImageCrop() {
		val intent = Intent(this, ImageCropActivity::class.java)
		cropActivityResultLauncher.launch(intent)
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val bitmapByteArray =
					intent?.getByteArrayExtra(ImageCropActivity.EXTRA_CROPPED_PROFILE_BYTE_ARRAY)
				bitmapByteArray?.let { channelSettingViewModel.onChangeChannelProfile(it) }
			}
		}

	private val manageActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				finish()
			}
		}

	private fun moveToHostManage() {
		val channelId = channelSettingViewModel.uiState.value.channel.roomId
		val intent = Intent(this, HostManageActivity::class.java)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		manageActivityResultLauncher.launch(intent)
	}

	private fun moveToSubHostManage() {
		val channelId = channelSettingViewModel.uiState.value.channel.roomId
		val intent = Intent(this, SubHostManageActivity::class.java)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		manageActivityResultLauncher.launch(intent)
	}

	private fun showChannelExitWarningDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_EXIT_WARNING)
		if (existingFragment != null) return

		val dialog = ChannelExitWarningDialog(
			clientAuthority = ChannelMemberAuthority.HOST,
			onClickOkBtn = { channelSettingViewModel.onClickChannelExitDialogBtn() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_EXIT_WARNING)
	}

	private fun showChannelCapacityDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_CAPACITY_DIALOG)
		if (existingFragment != null) return

		val dialog = ChannelCapacitySettingDialog(
			currentCapacity = channelSettingViewModel.uiState.value.channel.roomCapacity ?: return,
			onClickOkBtn = { newCapacity ->
				channelSettingViewModel.onClickChannelCapacityDialogBtn(newCapacity)
			}
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_CAPACITY_DIALOG)
	}

	private fun exitChannel() {
		setResult(RESULT_OK, intent)
		finish()
	}

	private fun handleEvent(event: ChannelSettingUiEvent) {
		when (event) {
			ChannelSettingUiEvent.MoveBack -> finish()
			ChannelSettingUiEvent.PermissionCheck -> startChannelProfileEdit()
			ChannelSettingUiEvent.ShowChannelExitWarningDialog -> showChannelExitWarningDialog()
			is ChannelSettingUiEvent.MakeToast -> makeToast(event.stringId)
			ChannelSettingUiEvent.ShowChannelCapacityDialog -> showChannelCapacityDialog()
			ChannelSettingUiEvent.MoveHostManage -> moveToHostManage()
			ChannelSettingUiEvent.MoveSubHostManage -> moveToSubHostManage()
			ChannelSettingUiEvent.ExitChannel -> exitChannel()
		}
	}

	companion object {
		private const val DIALOG_TAG_CHANNEL_EXIT_WARNING = "DIALOG_TAG_CHANNEL_EXIT_WARNING"
		private const val DIALOG_TAG_CHANNEL_CAPACITY_DIALOG = "DIALOG_TAG_CHANNEL_CAPACITY_DIALOG"
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
	}
}