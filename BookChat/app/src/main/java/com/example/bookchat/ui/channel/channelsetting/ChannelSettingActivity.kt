package com.example.bookchat.ui.channel.channelsetting

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.example.bookchat.ui.imagecrop.model.ImageCropPurpose
import com.example.bookchat.ui.mypage.useredit.dialog.ProfileEditDialog
import com.example.bookchat.utils.MakeChannelImgSizeManager
import com.example.bookchat.utils.image.bitmap.getImageBitmap
import com.example.bookchat.utils.image.deleteImageCache
import com.example.bookchat.utils.image.loadChangedChannelProfile
import com.example.bookchat.utils.makeToast
import com.example.bookchat.utils.permissions.galleryPermissions
import com.example.bookchat.utils.permissions.getPermissionsLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelSettingActivity : AppCompatActivity() {
	private lateinit var binding: ActivityChannelSettingBinding

	private val channelSettingViewModel by viewModels<ChannelSettingViewModel>()

	private val permissionsLauncher = this.getPermissionsLauncher(
		onSuccess = { moveToImageCrop() },
		onDenied = {
			makeToast(R.string.gallery_permission_denied)
		},
		onExplained = {
			makeToast(R.string.gallery_permission_explained)
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
			val uri = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
			intent.data = uri
			startActivity(intent)
		}
	)

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
		makeChannelImgSizeManager.setMakeChannelImgSize(binding.channelImgIv)
	}

	private fun setViewState(uiState: ChannelSettingUiState) {
		setChannelTitleEditTextState(uiState)
		setChannelTagEditTextState(uiState)
		setApplyChannelChangeBtnState(uiState)
		setChannelImage(uiState)
	}

	private fun setChannelTitleEditTextState(uiState: ChannelSettingUiState) {
		with(binding.channelTitleEt) {
			if (uiState.newTitle != text.toString()) {
				setText(uiState.newTitle)
				setSelection(uiState.newTitle.length)
			}
		}
	}

	private fun setChannelTagEditTextState(uiState: ChannelSettingUiState) {
		with(binding.channelTagEt) {
			if (uiState.newTags != text.toString()) {
				setText(uiState.newTags)
				setSelection(uiState.newTags.length)
			}
		}
	}

	private fun setApplyChannelChangeBtnState(uiState: ChannelSettingUiState) {
		with(binding.applyChannelChangeBtn) {
			if (uiState.isPossibleChangeChannel) {
				isEnabled = true
				setTextColor(Color.parseColor("#000000"))
			} else {
				isEnabled = false
				setTextColor(Color.parseColor("#D9D9D9"))
			}
		}
	}

	private fun setChannelImage(uiState: ChannelSettingUiState) {
		binding.channelImgIv.loadChangedChannelProfile(
			imageUrl = uiState.channel.roomImageUri,
			channelDefaultImageType = uiState.channel.defaultRoomImageType,
			bitmap = uiState.newProfileImage,
		)
	}

	private fun startChannelProfileEdit() {
		permissionsLauncher.launch(galleryPermissions)
	}

	private fun moveToImageCrop() {
		val intent = Intent(this, ImageCropActivity::class.java)
		intent.putExtra(ImageCropActivity.EXTRA_CROP_PURPOSE, ImageCropPurpose.CHANNEL_PROFILE)
		cropActivityResultLauncher.launch(intent)
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val uri = result.data?.getStringExtra(ImageCropActivity.EXTRA_CROPPED_IMAGE_CACHE_URI)
					?: return@registerForActivityResult
				getCroppedImageBitmap(uri)
			}
		}

	private fun getCroppedImageBitmap(uri: String) = lifecycleScope.launch {
		val croppedImageBitmap = uri.getImageBitmap(this@ChannelSettingActivity) ?: return@launch
		channelSettingViewModel.onChangeChannelProfile(croppedImageBitmap)
		deleteImageCache(uri)
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

	private fun showProfileEditDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_PROFILE_EDIT)
		if (existingFragment != null) return

		val dialog = ProfileEditDialog(
			onSelectDefaultImage = { channelSettingViewModel.onSelectDefaultProfileImage() },
			onSelectGallery = { channelSettingViewModel.onSelectGallery() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_PROFILE_EDIT)
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
		setResult(RESULT_CODE_USER_CHANNEL_EXIT, intent)
		finish()
	}

	private fun handleEvent(event: ChannelSettingUiEvent) {
		when (event) {
			ChannelSettingUiEvent.MoveBack -> finish()
			ChannelSettingUiEvent.MoveToGallery -> startChannelProfileEdit()
			ChannelSettingUiEvent.ShowChannelExitWarningDialog -> showChannelExitWarningDialog()
			is ChannelSettingUiEvent.MakeToast -> makeToast(event.stringId)
			ChannelSettingUiEvent.ShowChannelCapacityDialog -> showChannelCapacityDialog()
			ChannelSettingUiEvent.MoveHostManage -> moveToHostManage()
			ChannelSettingUiEvent.MoveSubHostManage -> moveToSubHostManage()
			ChannelSettingUiEvent.ExitChannel -> exitChannel()
			ChannelSettingUiEvent.ShowProfileEditDialog -> showProfileEditDialog()
		}
	}

	companion object {
		private const val DIALOG_TAG_CHANNEL_EXIT_WARNING = "DIALOG_TAG_CHANNEL_EXIT_WARNING"
		private const val DIALOG_TAG_CHANNEL_CAPACITY_DIALOG = "DIALOG_TAG_CHANNEL_CAPACITY_DIALOG"
		private const val DIALOG_TAG_PROFILE_EDIT = "DIALOG_TAG_USER_PROFILE_EDIT"
		const val RESULT_CODE_USER_CHANNEL_EXIT = 100
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		private const val SCHEME_PACKAGE = "package"
	}
}