package com.example.bookchat.ui.search.channelInfo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelInfoBinding
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.ui.search.channelInfo.dialog.BannedChannelNoticeDialog
import com.example.bookchat.ui.search.channelInfo.dialog.FullChannelNoticeDialog
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.getFormattedAbstractDateTimeText
import com.example.bookchat.utils.image.loadChannelProfile
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.image.loadUserProfile
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelInfoActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelInfoBinding
	private val channelInfoViewModel by viewModels<ChannelInfoViewModel>()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

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
		binding.bookAuthorTv.text = uiState.channel.bookAuthorsString
		binding.bookTitleTv.text = uiState.channel.bookTitle
		binding.channelTitle.text = uiState.channel.roomName
		binding.channelTagsTv.text = uiState.channel.tagsString
		binding.channelHostNickNameTv.text = uiState.channel.host.nickname
		if (uiState.channel.isBanned) showBannedChannelNoticeDialog()
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
					getFormattedAbstractDateTimeText(it.dispatchTime)
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
			if (state.channel.isBanned) {
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

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(this, ChannelActivity::class.java)
		intent.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun handleEvent(event: ChannelInfoEvent) {
		when (event) {
			is ChannelInfoEvent.MoveToBack -> finish()
			is ChannelInfoEvent.MoveToChannel -> moveToChannel(event.channelId)
			is ChannelInfoEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.enterChannelBtn
			)

			ChannelInfoEvent.ShowFullChannelDialog -> showFullChannelNoticeDialog()
		}
	}

	companion object {
		private const val DIALOG_TAG_BANNED_CHANNEL_NOTICE = "DIALOG_TAG_BANNED_CHANNEL_NOTICE"
		private const val DIALOG_TAG_FULL_CHANNEL_NOTICE = "DIALOG_TAG_FULL_CHANNEL_NOTICE"
	}
}