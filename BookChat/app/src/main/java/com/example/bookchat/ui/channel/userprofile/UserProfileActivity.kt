package com.example.bookchat.ui.channel.userprofile

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityUserProfileBinding
import com.example.bookchat.ui.channel.userprofile.dialog.UserBanWarningDialog
import com.example.bookchat.utils.image.loadChannelProfile
import com.example.bookchat.utils.image.loadUserProfile
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUserProfileBinding
	private val userProfileViewModel by viewModels<UserProfileViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)
		binding.lifecycleOwner = this
		binding.viewmodel = userProfileViewModel
		observeUiState()
		observeUiEvent()
	}

	fun observeUiState() = lifecycleScope.launch {
		userProfileViewModel.uiState.collect { uiState ->
			setViewState(uiState)
		}
	}

	fun observeUiEvent() = lifecycleScope.launch {
		userProfileViewModel.eventFlow.collect { handleEvent(it) }
	}

	private fun setViewState(uiState: UserProfileUiState) {
		with(binding) {
			adminsCanSeeGroup.visibility =
				if (uiState.isClientAdmin
					&& uiState.isTargetUserHost.not()
					&& uiState.isTargetUserExistInChannel
					&& (uiState.targetUser.id != uiState.client.id)
				) View.VISIBLE else View.GONE

			hostCrown.visibility =
				if (uiState.isTargetUserHost) View.VISIBLE else View.GONE
			subHostCrown.visibility =
				if (uiState.isTargetUserSubHost) View.VISIBLE else View.GONE
			chatRoomBackgroundIv.loadChannelProfile(
				imageUrl = uiState.channel.roomImageUri,
				channelDefaultImageType = uiState.channel.defaultRoomImageType
			)
			userProfileIv.loadUserProfile(
				imageUrl = uiState.targetUser.profileImageUrl,
				userDefaultProfileType = uiState.targetUser.defaultProfileImageType
			)
		}
	}

	private fun handleEvent(event: UserProfileUiEvent) {
		when (event) {
			UserProfileUiEvent.MoveBack -> finish()
			UserProfileUiEvent.ShowUserBanWarningDialog -> showUserBanDialog()
			is UserProfileUiEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	private fun showUserBanDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_USER_BAN_WARNING)
		if (existingFragment != null) return

		val dialog = UserBanWarningDialog(
			onClickOkBtn = { userProfileViewModel.onClickUserBanDialogBtn() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_USER_BAN_WARNING)
	}

	companion object {
		const val DIALOG_TAG_USER_BAN_WARNING = "DIALOG_TAG_USER_BAN_WARNING"
	}

}