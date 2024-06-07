package com.example.bookchat.ui.channel.chatting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelBinding
import com.example.bookchat.domain.model.ChannelMemberAuthority
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.SocketState
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.channel.channelsetting.ChannelSettingActivity
import com.example.bookchat.ui.channel.chatting.adapter.ChatItemAdapter
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.ui.channel.drawer.adapter.ChannelDrawerAdapter
import com.example.bookchat.ui.channel.drawer.dialog.ChannelBannedUserNoticeDialog
import com.example.bookchat.ui.channel.drawer.dialog.ChannelExitWarningDialog
import com.example.bookchat.ui.channel.drawer.dialog.ExplodedChannelNoticeDialog
import com.example.bookchat.ui.channel.drawer.mapper.toUser
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem
import com.example.bookchat.ui.channel.userprofile.UserProfileActivity
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.isOnListBottom
import com.example.bookchat.utils.isOnListTop
import com.example.bookchat.utils.isVisiblePosition
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelBinding
	private val channelViewModel by viewModels<ChannelViewModel>()

	@Inject
	lateinit var chatItemAdapter: ChatItemAdapter

	@Inject
	lateinit var channelDrawerAdapter: ChannelDrawerAdapter

	@Inject
	lateinit var chatItemDecoration: ChatItemDecoration

	private lateinit var linearLayoutManager: LinearLayoutManager

	private val imm by lazy {
		getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_channel)
		binding.lifecycleOwner = this
		binding.viewmodel = channelViewModel
		setBackPressedDispatcher()
		initLayoutManager()
		initAdapter()
		initRcv()
		initViewState()
		observeUiState()
		observeUiEvent()
	}

	override fun onStart() {
		super.onStart()
		channelViewModel.onStartScreen()
	}

	override fun onStop() {
		super.onStop()
		channelViewModel.onStopScreen()
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelViewModel.uiState.collect { uiState ->
			chatItemAdapter.submitList(uiState.chats)
			channelDrawerAdapter.submitList(uiState.drawerItems)
			setViewState(uiState)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		channelViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		with(binding.chatInputEt) {
			if (channelViewModel.uiState.value.channel.isAvailableChannel) isEnabled = true
			addTextChangedListener { text ->
				val message = text?.toString() ?: return@addTextChangedListener
				channelViewModel.onChangeEnteredMessage(message)
			}
		}
	}

	private fun setViewState(uiState: ChannelUiState) {
		setMessageBarState(uiState)
		setBottomScrollBtnState(uiState)
		setNewChatNoticeState(uiState)
		setSocketConnectionUiState(uiState)
		setExplodedChannelUiState(uiState)
		setBannedClientUIState(uiState)
		setChannelSettingBtnUiState(uiState)
	}

	private fun setBottomScrollBtnState(uiState: ChannelUiState) {
		binding.chatBottomScrollBtn.visibility =
			if (uiState.isPossibleToShowBottomScrollBtn) View.VISIBLE else View.GONE
	}

	private fun setSocketConnectionUiState(uiState: ChannelUiState) {
		Log.d(
			TAG,
			"ChannelActivity: setSocketConnectionUiState() - uiState.socketState : ${uiState.socketState}"
		)
		when (uiState.socketState) {
			SocketState.CONNECTING -> {
				binding.socketStateBar.setText(R.string.connecting_network)
				binding.socketStateBar.setBackgroundColor(Color.parseColor("#666666"))
				binding.socketStateBar.visibility = View.VISIBLE
			}

			SocketState.CONNECTED -> {
				binding.socketStateBar.setText(R.string.connected_network)
				binding.socketStateBar.setBackgroundColor(Color.parseColor("#5648FF"))
				lifecycleScope.launch {
					delay(SOCKET_CONNECTION_SUCCESS_BAR_EXPOSURE_TIME)
					binding.socketStateBar.visibility =
						if (channelViewModel.uiState.value.socketState == uiState.socketState)
							View.GONE else View.VISIBLE
				}
			}

			SocketState.FAILURE,
			SocketState.NEED_RECONNECTION,
			SocketState.DISCONNECTED,
			-> {
				binding.socketStateBar.setText(R.string.offline_network)
				binding.socketStateBar.setBackgroundColor(Color.parseColor("#666666"))
				binding.socketStateBar.visibility = View.VISIBLE
			}
		}
	}

	private fun setMessageBarState(uiState: ChannelUiState) {
		with(binding.chatInputEt) {
			if (uiState.enteredMessage != text.toString()) {
				setText(uiState.enteredMessage)
				setSelection(uiState.enteredMessage.length)
			}
		}
	}

	private fun setNewChatNoticeState(uiState: ChannelUiState) {
		binding.newChatNoticeLayout.layout.visibility =
			if (uiState.newChatNotice != null) View.VISIBLE else View.INVISIBLE
	}

	private fun setChannelSettingBtnUiState(uiState: ChannelUiState) {
		with(binding.chatDrawerLayout.channelSettingBtn) {
			visibility = if (uiState.isClientHost) View.VISIBLE else View.GONE
		}
	}

	private fun initLayoutManager() {
		linearLayoutManager = LinearLayoutManager(this)
		linearLayoutManager.reverseLayout = true
	}

	private fun initAdapter() {
		chatItemAdapter.onClickUserProfile = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.AnotherUser)
			channelViewModel.onClickUserProfile(item.sender!!)
		}
		chatItemAdapter.onClickFailedChatDeleteBtn = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.MyChat)
			channelViewModel.onClickFailedChatDeleteBtn(item.chatId)
		}
		chatItemAdapter.onClickFailedChatRetryBtn = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.MyChat)
			channelViewModel.onClickFailedChatRetryBtn(item.chatId)
		}
		chatItemAdapter.registerAdapterDataObserver(adapterDataObserver)

		channelDrawerAdapter.onClickUserProfile = { itemPosition ->
			val userItem = (channelDrawerAdapter.currentList[itemPosition] as ChannelDrawerItem.UserItem)
			channelViewModel.onClickUserProfile(userItem.toUser())
		}
	}

	private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			super.onItemRangeInserted(positionStart, itemCount)
			scrollToLastReadNoticeIfExists()
			if (itemCount <= 2) scrollToBottomIfOnBottom()
			removeNewChatNoticeIfAppearsOnScreen()
			removeLastReadNoticeScrollFlagIfAppearsOnScreen()
		}
	}

	private fun scrollToLastReadNoticeIfExists() {
		val lastReadChatNoticeIndex = chatItemAdapter.lastReadChatNoticeIndex
		if (isPossibleScrollToLastReadNotice(lastReadChatNoticeIndex).not()) return
		scrollToPosition(lastReadChatNoticeIndex)
	}

	private fun isPossibleScrollToLastReadNotice(lastReadChatNoticeIndex: Int): Boolean {
		return chatItemAdapter.currentList.isNotEmpty()
						&& (channelViewModel.uiState.value.socketState == SocketState.CONNECTED)
						&& (lastReadChatNoticeIndex != -1 &&
						channelViewModel.uiState.value.needToScrollToLastReadChat)
	}

	/** 새로운 채팅이 들어오는 경우 스크롤 position이 고정되는 현상이 있어서 아래로 스크롤 */
	private fun scrollToBottomIfOnBottom() {
		if (channelViewModel.uiState.value.isNewerChatFullyLoaded.not()
			|| linearLayoutManager.isOnListBottom().not()
		) return
		scrollToBottom()
	}

	private fun initRcv() {
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				removeNewChatNoticeIfAppearsOnScreen()
				removeLastReadNoticeScrollFlagIfAppearsOnScreen()
				channelViewModel.onChangeStateOfLookingAtBottom(linearLayoutManager.isOnListBottom())
				when {
					linearLayoutManager.isOnListTop() -> channelViewModel.onReachedTopChat()
					linearLayoutManager.isOnListBottom() -> channelViewModel.onReachedBottomChat()
				}
			}
		}

		binding.chattingRcv.apply {
			adapter = chatItemAdapter
			setHasFixedSize(true)
			addItemDecoration(chatItemDecoration)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}

		binding.chatDrawerLayout.chatroomDrawerRcv.apply {
			adapter = channelDrawerAdapter
			layoutManager = LinearLayoutManager(this@ChannelActivity)
			setHasFixedSize(true)
		}
	}

	private fun removeNewChatNoticeIfAppearsOnScreen() {
		val newestChatNotMineIndex = chatItemAdapter.newestChatNotMineIndex
		if (linearLayoutManager.isVisiblePosition(newestChatNotMineIndex).not()) return
		val item = chatItemAdapter.currentList[newestChatNotMineIndex] as ChatItem.Message
		channelViewModel.onReadNewestChatNotMineInList(item)
	}

	private fun removeLastReadNoticeScrollFlagIfAppearsOnScreen() {
		val lastReadChatNoticeIndex = chatItemAdapter.lastReadChatNoticeIndex
		if (linearLayoutManager.isVisiblePosition(lastReadChatNoticeIndex).not()) return
		channelViewModel.onReadLastReadNotice()
	}

	private fun moveToUserProfile(user: User) {
		val channelId = channelViewModel.uiState.value.channel.roomId ?: return
		val intent = Intent(this, UserProfileActivity::class.java)
			.putExtra(EXTRA_USER_ID, user.id)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private val channelSettingResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				finish()
			}
		}

	private fun moveChannelSetting() {
		val channelId = channelViewModel.uiState.value.channel.roomId ?: return
		val intent = Intent(this, ChannelSettingActivity::class.java)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		channelSettingResultLauncher.launch(intent)
	}

	private fun setExplodedChannelUiState(uiState: ChannelUiState) {
		if (uiState.channel.isExploded.not()) return
		showExplodedChannelNoticeDialog()
		with(binding.chatInputEt) {
			isEnabled = false
			setText("")
			setHint(R.string.unavailable_channel_edittext_hint)
		}
	}

	private fun setBannedClientUIState(uiState: ChannelUiState) {
		if (uiState.channel.isBanned.not()) return
		showBannedClientNoticeDialog()
		with(binding.chatInputEt) {
			isEnabled = false
			setText("")
			setHint(R.string.unavailable_channel_edittext_hint)
		}
	}

	private fun showBannedClientNoticeDialog() {
		Log.d(TAG, "ChannelActivity: showBannedClientNoticeDialog() - called")
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE)
		if (existingFragment != null) return
		val dialog = ChannelBannedUserNoticeDialog()
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE)
	}

	private fun showExplodedChannelNoticeDialog() {
		Log.d(TAG, "ChannelActivity: showExplodedChannelNoticeDialog() - called")
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_EXPLODED_CHANNEL_NOTICE)
		if (existingFragment != null) return
		val dialog = ExplodedChannelNoticeDialog()
		dialog.show(supportFragmentManager, DIALOG_TAG_EXPLODED_CHANNEL_NOTICE)
	}

	private fun showChannelExitWarningDialog(clientAuthority: ChannelMemberAuthority) {
		Log.d(TAG, "ChannelActivity: showChannelExitWarningDialog() - called")
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_EXIT_WARNING)
		if (existingFragment != null) return
		val dialog = ChannelExitWarningDialog(
			clientAuthority = clientAuthority,
			onClickOkBtn = { channelViewModel.onClickChannelExitDialogBtn() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_EXIT_WARNING)
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
				binding.drawerLayout.closeDrawer(GravityCompat.END)
				return@addCallback
			}
			channelViewModel.onClickBackBtn()
		}
	}

	private fun scrollToPosition(position: Int) {
		val offset = binding.chattingRcv.height / 2
		linearLayoutManager.scrollToPositionWithOffset(position, offset)
	}

	private fun scrollToBottom() {
		if (isPossibleScrollToBottom().not()) return
		binding.chattingRcv.scrollToPosition(0)
	}

	private fun isPossibleScrollToBottom(): Boolean {
		return chatItemAdapter.currentList.isNotEmpty()
						&& channelViewModel.uiState.value.isNewerChatFullyLoaded
	}

	/** 현재 로드된 채팅 기준으로 가장 최신 채팅이 화면상에 보이지 않는다면 무조건 notice
	 * 지금 보고있는 화면상 가장 상단의 채팅이 newestChatNotFailedIndex보다 인덱스가 낮은 경우는
	 * FailedChat으로 화면이 가득찬 경우로 인지하고 띄우지 않음 */
	private fun checkIfNewChatNoticeIsRequired(channelLastChat: Chat) {
		val newestChatNotFailedIndex = chatItemAdapter.newestChatNotFailedIndex
		val lvip = linearLayoutManager.findLastVisibleItemPosition()

		if (channelViewModel.uiState.value.isNewerChatFullyLoaded.not()) {
			channelViewModel.onNeedNewChatNotice(channelLastChat)
			return
		}

		if (linearLayoutManager.isVisiblePosition(newestChatNotFailedIndex)
			|| (lvip <= newestChatNotFailedIndex)
		) return

		channelViewModel.onNeedNewChatNotice(channelLastChat)
	}

	private fun handleEvent(event: ChannelEvent) {
		when (event) {
			ChannelEvent.MoveBack -> finish()
			ChannelEvent.CaptureChannel -> {}
			ChannelEvent.OpenOrCloseDrawer -> openOrCloseDrawer()
			ChannelEvent.ScrollToBottom -> scrollToBottom()
			is ChannelEvent.MoveUserProfile -> moveToUserProfile(event.user)
			is ChannelEvent.MakeToast -> makeToast(event.stringId)
			is ChannelEvent.NewChatOccurEvent -> checkIfNewChatNoticeIsRequired(event.chat)
			ChannelEvent.MoveChannelSetting -> moveChannelSetting()
			is ChannelEvent.ShowChannelExitWarningDialog ->
				showChannelExitWarningDialog(event.clientAuthority)
		}
	}

	private fun closeKeyboard() {
		binding.chatInputEt.clearFocus()
		imm.hideSoftInputFromWindow(
			binding.chatInputEt.windowToken,
			InputMethodManager.HIDE_NOT_ALWAYS
		)
	}

	private fun openOrCloseDrawer() {
		with(binding.drawerLayout) {
			if (isDrawerOpen(GravityCompat.END)) {
				closeDrawer(GravityCompat.END)
				return
			}
			closeKeyboard()
			openDrawer(GravityCompat.END)
		}
	}

	companion object {
		const val EXTRA_USER_ID = "EXTRA_USER_ID"
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		private const val DIALOG_TAG_CHANNEL_EXIT_WARNING = "DIALOG_TAG_CHANNEL_EXIT_WARNING"
		private const val DIALOG_TAG_EXPLODED_CHANNEL_NOTICE = "DIALOG_TAG_EXPLODED_CHANNEL_NOTICE"
		private const val DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE =
			"DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE"
		private const val SOCKET_CONNECTION_SUCCESS_BAR_EXPOSURE_TIME = 1500L
	}
}