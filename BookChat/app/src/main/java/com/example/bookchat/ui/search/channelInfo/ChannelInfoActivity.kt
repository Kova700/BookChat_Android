package com.example.bookchat.ui.search.channelInfo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelInfoBinding
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment.Companion.EXTRA_CHANNEL_ID
import com.example.bookchat.ui.search.channelInfo.dialog.BannedChannelNoticeDialog
import com.example.bookchat.ui.search.channelInfo.dialog.FullChannelNoticeDialog
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DateManager
import com.example.bookchat.utils.makeToast
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
		binding = DataBindingUtil.setContentView(this, R.layout.activity_channel_info)
		binding.lifecycleOwner = this
		binding.viewmodel = channelInfoViewModel
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
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	private fun setViewState(state: ChannelInfoUiState) {
		setDateTimeText(state)
		setRoomMemberCountText(state)
		setChannelEnterBtnState(state)
		if (state.channel.isBanned) showBannedChannelNoticeDialog()
	}

	private fun setDateTimeText(state: ChannelInfoUiState) {
		state.channel.lastChat?.let {
			binding.chatRoomLastActiveTv.text =
				DateManager.getFormattedAbstractDateTimeText(it.dispatchTime)
		}
	}

	private fun setRoomMemberCountText(state: ChannelInfoUiState) {
		binding.roomMemberCount.text =
			getString(
				R.string.room_member_count,
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
			is ChannelInfoEvent.MakeToast -> makeToast(event.stringId)
			ChannelInfoEvent.ShowFullChannelDialog -> showFullChannelNoticeDialog()
		}
	}

	companion object {
		private const val DIALOG_TAG_BANNED_CHANNEL_NOTICE = "DIALOG_TAG_BANNED_CHANNEL_NOTICE"
		private const val DIALOG_TAG_FULL_CHANNEL_NOTICE = "DIALOG_TAG_FULL_CHANNEL_NOTICE"
	}
}