package com.kova700.bookchat.feature.search.channelInfo

import android.graphics.Color
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.navigation.ChannelNavigator
import com.kova700.bookchat.feature.search.channelInfo.dialog.BannedChannelNoticeDialog
import com.kova700.bookchat.feature.search.channelInfo.dialog.FullChannelNoticeDialog
import com.kova700.bookchat.feature.search.channelInfo.dialog.InvalidChannelNoticeDialog
import com.kova700.bookchat.feature.search.databinding.ActivityChannelInfoBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.date.toFormattedAbstractDateTimeText
import com.kova700.bookchat.util.image.image.loadChannelProfile
import com.kova700.bookchat.util.image.image.loadUrl
import com.kova700.bookchat.util.image.image.loadUserProfile
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelInfoActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelInfoBinding
	private val channelInfoViewModel by viewModels<ChannelInfoViewModel>()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var channelNavigator: ChannelNavigator

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityChannelInfoBinding.inflate(layoutInflater)
		setContentView(binding.root)
		observeUiState()
		observeUiEvent()
		initViewState()
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelInfoViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		channelInfoViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding) {
			backBtn.setOnClickListener { channelInfoViewModel.onClickBackBtn() }
			enterChannelBtn.setOnClickListener { channelInfoViewModel.onClickEnterBtn() }
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	private fun setViewState(uiState: ChannelInfoUiState) {
		setChannelImage(uiState)
		setBookCoverImage(uiState)
		setHostProfileImage(uiState)
		setDateTimeText(uiState)
		setRoomMemberCountText(uiState)
		setChannelEnterBtnState(uiState)
		with(binding) {
			bookAuthorTv.text = uiState.channel.bookAuthorsString
			bookTitleTv.text = uiState.channel.bookTitle
			channelTitle.text = uiState.channel.roomName
			channelTagsTv.text = uiState.channel.tagsString
			channelHostNickNameTv.text = uiState.channel.host.nickname
		}
		if (uiState.isBannedChannel) showBannedChannelNoticeDialog()
		binding.progressBar.visibility = if (uiState.isLoading) VISIBLE else GONE
	}

	private fun setHostProfileImage(uiState: ChannelInfoUiState) {
		binding.hostProfileImgIv.loadUserProfile(
			imageUrl = uiState.channel.host.profileImageUrl,
			userDefaultProfileType = uiState.channel.host.defaultProfileImageType
		)
	}

	private fun setBookCoverImage(state: ChannelInfoUiState) {
		binding.bookImg.loadUrl(
			url = state.channel.bookCoverImageUrl,
			errorResId = R.drawable.empty_img
		)
	}

	private fun setChannelImage(state: ChannelInfoUiState) {
		binding.channelBackgroundIv.loadChannelProfile(
			imageUrl = state.channel.roomImageUri,
			channelDefaultImageType = state.channel.defaultRoomImageType
		)
	}

	private fun setDateTimeText(state: ChannelInfoUiState) {
		state.channel.lastChat?.let {
			binding.channelLastActiveTv.text =
				getString(
					R.string.channel_last_active_time,
					it.dispatchTime.toFormattedAbstractDateTimeText()
				)
		}
	}

	private fun setRoomMemberCountText(state: ChannelInfoUiState) {
		binding.channelMemberCount.text =
			getString(
				R.string.current_room_member_count,
				state.channel.roomMemberCount,
				state.channel.roomCapacity
			)
	}

	private fun setChannelEnterBtnState(state: ChannelInfoUiState) {
		with(binding.enterChannelBtn) {
			if (state.isBannedChannel) {
				setBackgroundColor(Color.parseColor("#D9D9D9"))
				setText(R.string.banned_channel)
				isEnabled = false
				return
			}
			setBackgroundColor(Color.parseColor("#5648FF"))
			setText(R.string.enter)
			isEnabled = true
		}
	}

	private fun showBannedChannelNoticeDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_BANNED_CHANNEL_NOTICE)
		if (existingFragment != null) return
		val dialog = BannedChannelNoticeDialog()
		dialog.show(supportFragmentManager, DIALOG_TAG_BANNED_CHANNEL_NOTICE)
	}

	private fun showFullChannelNoticeDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_FULL_CHANNEL_NOTICE)
		if (existingFragment != null) return
		val dialog = FullChannelNoticeDialog()
		dialog.show(supportFragmentManager, DIALOG_TAG_FULL_CHANNEL_NOTICE)
	}

	private fun showInvalidChannelNoticeDialog() {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_INVALID_CHANNEL_NOTICE)
		if (existingFragment != null) return
		val dialog = InvalidChannelNoticeDialog()
		dialog.show(supportFragmentManager, DIALOG_TAG_INVALID_CHANNEL_NOTICE)
	}

	private fun moveToChannel(channelId: Long) {
		channelNavigator.navigate(
			currentActivity = this,
			channelId = channelId,
		)
	}

	private fun handleEvent(event: ChannelInfoEvent) {
		when (event) {
			is ChannelInfoEvent.MoveToBack -> finish()
			is ChannelInfoEvent.MoveToChannel -> moveToChannel(event.channelId)
			is ChannelInfoEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.enterChannelBtn
			)

			ChannelInfoEvent.ShowFullChannelNoticeDialog -> showFullChannelNoticeDialog()
			ChannelInfoEvent.ShowExplodedChannelDialog -> showInvalidChannelNoticeDialog()
		}
	}

	companion object {
		private const val DIALOG_TAG_BANNED_CHANNEL_NOTICE = "DIALOG_TAG_BANNED_CHANNEL_NOTICE"
		private const val DIALOG_TAG_FULL_CHANNEL_NOTICE = "DIALOG_TAG_FULL_CHANNEL_NOTICE"
		private const val DIALOG_TAG_INVALID_CHANNEL_NOTICE = "DIALOG_TAG_INVALID_CHANNEL_NOTICE"
	}
}