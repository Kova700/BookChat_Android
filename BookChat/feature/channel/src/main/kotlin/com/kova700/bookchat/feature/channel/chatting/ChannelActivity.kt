package com.kova700.bookchat.feature.channel.chatting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.core.data.chat.external.model.Chat
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.core.stomp.chatting.external.model.SocketState
import com.kova700.bookchat.feature.channel.channelsetting.ChannelSettingActivity
import com.kova700.bookchat.feature.channel.channelsetting.ChannelSettingActivity.Companion.RESULT_CODE_USER_CHANNEL_EXIT
import com.kova700.bookchat.feature.channel.chatting.adapter.ChatItemAdapter
import com.kova700.bookchat.feature.channel.chatting.capture.captureItems
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem
import com.kova700.bookchat.feature.channel.chatting.wholetext.ChatWholeTextActivity
import com.kova700.bookchat.feature.channel.chatting.wholetext.ChatWholeTextViewmodel
import com.kova700.bookchat.feature.channel.databinding.ActivityChannelBinding
import com.kova700.bookchat.feature.channel.drawer.adapter.ChannelDrawerAdapter
import com.kova700.bookchat.feature.channel.drawer.dialog.ChannelBannedUserNoticeDialog
import com.kova700.bookchat.feature.channel.drawer.dialog.ChannelExitWarningDialog
import com.kova700.bookchat.feature.channel.drawer.dialog.ExplodedChannelNoticeDialog
import com.kova700.bookchat.feature.channel.drawer.mapper.toUser
import com.kova700.bookchat.feature.channel.drawer.model.ChannelDrawerItem
import com.kova700.bookchat.feature.channel.userprofile.UserProfileActivity
import com.kova700.bookchat.util.Constants.TAG
import com.kova700.bookchat.util.image.image.loadUserProfile
import com.kova700.bookchat.util.recyclerview.isOnHigherPosition
import com.kova700.bookchat.util.recyclerview.isOnListBottom
import com.kova700.bookchat.util.recyclerview.isOnListTop
import com.kova700.bookchat.util.recyclerview.isVisiblePosition
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 폭발한 채팅방 나가기 할 때, 서버 요청 실패하더라도 괜찮게 예외처리 (soft삭제라 서버에 없는 채팅방 일 수도 있음)
//TODO : 유저가 해당 채팅방을 안보고 채널 목록을 보고 있는 상황에서 채팅방에 강퇴당한 후, 채팅방을 누르고 들어오면 아무 UI가 안뜸 (강퇴당했다는 UI 마저도 안뜸)
@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelBinding
	private val channelViewModel by viewModels<ChannelViewModel>()

	@Inject
	lateinit var chatItemAdapter: ChatItemAdapter

	@Inject
	lateinit var channelDrawerAdapter: ChannelDrawerAdapter

	private lateinit var linearLayoutManager: LinearLayoutManager

	private val clipboardManager by lazy {
		getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	}

	private val imm by lazy {
		getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityChannelBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setBackPressedDispatcher()
		initViewState()
		observeUiState()
		observeUiEvent()
		observeCaptureIds()
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

	private fun observeCaptureIds() = lifecycleScope.launch {
		channelViewModel.captureIds.collect { captureIds ->
			setCaptureViewState(captureIds)
		}
	}

	private fun setCaptureViewState(captureIds: Pair<Long, Long>?) {
		val (headerId, bottomId) = captureIds ?: Pair(null, null)
		with(binding.channelCaptureLayout) {
			if (headerId != null || bottomId != null) {
				channelScrapConfirmBtn.setTextColor(Color.parseColor("#000000"))
				channelScrapConfirmBtn.isEnabled = true
				backBtn.visibility = View.INVISIBLE
				channelScrapSelectCancelBtn.visibility = View.VISIBLE
				channelScrapExplanationCommentTv.setText(
					R.string.channel_scrap_explanation_comment_on_selected
				)
				return
			}

			channelScrapConfirmBtn.setTextColor(Color.parseColor("#A0A0A5"))
			channelScrapConfirmBtn.isEnabled = false
			backBtn.visibility = View.VISIBLE
			channelScrapSelectCancelBtn.visibility = View.GONE
			channelScrapExplanationCommentTv.setText(
				R.string.channel_scrap_explanation_comment
			)

		}
	}

	private fun initViewState() {
		initLayoutManager()
		initAdapter()
		initRcv()
		with(binding.chatInputEt) {
			if (channelViewModel.uiState.value.channel.isAvailableChannel) isEnabled = true
			addTextChangedListener { text ->
				val message = text?.toString() ?: return@addTextChangedListener
				channelViewModel.onChangeEnteredMessage(message)
			}
		}
		with(binding) {
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
			chatInputEt.setOnFocusChangeListener { _, hasFocus ->
				chatInputEt.maxLines = if (hasFocus) 4 else 1
			}
			backBtn.setOnClickListener { channelViewModel.onClickBackBtn() }
			menuBtn.setOnClickListener { channelViewModel.onClickMenuBtn() }
			bottomScrollBtn.setOnClickListener { channelViewModel.onClickScrollToBottomBtn() }
			captureBtn.setOnClickListener { channelViewModel.onClickCaptureBtn() }
			chatSendBtn.setOnClickListener { channelViewModel.onClickSendMessageBtn() }
		}
		with(binding.newChatNoticeLayout) {
			bottomNewChatNoticeCv.setOnClickListener { channelViewModel.onClickNewChatNotice() }
		}
		with(binding.channelCaptureLayout) {
			backBtn.setOnClickListener { channelViewModel.onClickCancelCapture() }
			channelScrapSelectCancelBtn.setOnClickListener { channelViewModel.onClickCancelCaptureSelection() }
			channelScrapConfirmBtn.setOnClickListener { channelViewModel.onClickCompleteCapture() }
		}
		with(binding.channelDrawerLayout) {
			channelExitBtn.setOnClickListener { channelViewModel.onClickChannelExitBtn() }
			channelSettingBtn.setOnClickListener { channelViewModel.onClickChannelSettingBtn() }
		}
	}

	private fun setViewState(uiState: ChannelUiState) {
		setMessageBarState(uiState)
		setNewChatNoticeState(uiState)
		setSocketConnectionUiState(uiState)
		setExplodedChannelUiState(uiState)
		setBannedClientUIState(uiState)
		setChannelSettingBtnUiState(uiState)
		setCaptureMode(uiState)
		with(binding) {
			channelTitle.text = uiState.channel.roomName
			roomMemberCount.text = uiState.channel.roomMemberCount.toString()
		}
		binding.progressBar.visibility = if (uiState.isInitLoading) View.VISIBLE else View.GONE
	}

	private fun setCaptureMode(uiState: ChannelUiState) {
		if (uiState.isCaptureMode) changeToCaptureMode()
		else changeToDefaultMode()
	}

	private var isGoneAnimatingSocketStateBar = false
	private fun setSocketConnectionUiState(uiState: ChannelUiState) {
		fun setVisible() {
			with(binding.socketStateBar) {
				if (visibility == View.VISIBLE) return
				visibility = View.VISIBLE
				animate()
					.translationY(0F)
					.scaleY(1f)
					.alpha(1f)
					.setDuration(300)
			}
		}

		fun setGone() {
			with(binding.socketStateBar) {
				if (channelViewModel.uiState.value.socketState == uiState.socketState) {
					if (isGoneAnimatingSocketStateBar || visibility == View.GONE) return
					isGoneAnimatingSocketStateBar = true
					animate()
						.translationY(-height.toFloat())
						.scaleY(0f)
						.alpha(0f)
						.setDuration(300)
						.withEndAction {
							visibility = View.GONE
							isGoneAnimatingSocketStateBar = false
						}
				} else visibility = View.VISIBLE
			}
		}

		when (uiState.socketState) {
			SocketState.CONNECTING -> {
				with(binding.socketStateBar) {
					setText(R.string.connecting_network)
					setBackgroundColor(Color.parseColor("#666666"))
					setVisible()
				}
			}

			SocketState.CONNECTED -> {
				with(binding.socketStateBar) {
					setText(R.string.connected_network)
					setBackgroundColor(Color.parseColor("#5648FF"))
					lifecycleScope.launch {
						delay(SOCKET_CONNECTION_SUCCESS_BAR_EXPOSURE_TIME)
						setGone()
					}
				}
			}

			SocketState.FAILURE,
			SocketState.NEED_RECONNECTION,
			SocketState.DISCONNECTED,
			-> {
				with(binding.socketStateBar) {
					setText(R.string.offline_network)
					setBackgroundColor(Color.parseColor("#666666"))
					setVisible()
				}
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
		binding.chatSendBtn.visibility =
			if (uiState.enteredMessage.isBlank()) View.GONE else View.VISIBLE
	}

	private fun setNewChatNoticeState(uiState: ChannelUiState) {
		with(binding.newChatNoticeLayout) {
			root.visibility =
				if (uiState.newChatNotice != null) View.VISIBLE else View.INVISIBLE
			userProfileIv.loadUserProfile(
				imageUrl = uiState.newChatNotice?.sender?.profileImageUrl,
				userDefaultProfileType = uiState.newChatNotice?.sender?.defaultProfileImageType
			)
			userNicknameTv.text = uiState.newChatNotice?.sender?.nickname
			messageTv.text = uiState.newChatNotice?.message
		}
	}

	private fun setChannelSettingBtnUiState(uiState: ChannelUiState) {
		with(binding.channelDrawerLayout.channelSettingBtn) {
			visibility = if (uiState.isClientHost) View.VISIBLE else View.GONE
		}
	}

	private fun initLayoutManager() {
		linearLayoutManager = LinearLayoutManager(this)
		linearLayoutManager.reverseLayout = true
	}

	private fun initAdapter() {
		chatItemAdapter.onSelectCaptureChat = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem)
			channelViewModel.onSelectCaptureChat(item.getCategoryId())
		}
		chatItemAdapter.onClickUserProfile = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.AnotherUser)
			channelViewModel.onClickUserProfile(item.sender!!)
		}
		chatItemAdapter.onClickFailedChatDeleteBtn = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.MyChat)
			channelViewModel.onClickFailedChatDeleteBtn(item.chatId)
		}
		chatItemAdapter.onClickMoveToWholeText = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.Message)
			channelViewModel.onClickMoveToWholeText(item.chatId)
		}
		chatItemAdapter.onClickFailedChatRetryBtn = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.MyChat)
			channelViewModel.onClickFailedChatRetryBtn(item.chatId)
		}
		chatItemAdapter.onLongClickChatItem = { position ->
			val item = (chatItemAdapter.currentList[position] as ChatItem.Message)
			channelViewModel.onLongClickChatItem(item.message)
		}
		channelDrawerAdapter.onClickUserProfile = { itemPosition ->
			val userItem = (channelDrawerAdapter.currentList[itemPosition] as ChannelDrawerItem.UserItem)
			channelViewModel.onClickUserProfile(userItem.toUser())
		}
		chatItemAdapter.registerAdapterDataObserver(adapterDataObserver)
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

	private var bottomScrollBtnAnimation: ViewPropertyAnimator? = null
	private val overshootInterpolator = OvershootInterpolator()
	private var isGoneAnimatingBottomScrollBtn = false
	private fun setBottomScrollBtnState() {
		fun setVisible() {
			with(binding.chatBottomScrollBtnCv) {
				if (isGoneAnimatingBottomScrollBtn.not() && visibility == View.VISIBLE) return
				isGoneAnimatingBottomScrollBtn = false
				bottomScrollBtnAnimation?.cancel()
				visibility = View.VISIBLE
				scaleX = 0f
				scaleY = 0f
				alpha = 0f
				bottomScrollBtnAnimation = animate()
					.scaleX(1f)
					.scaleY(1f)
					.alpha(1f)
					.setInterpolator(overshootInterpolator)
					.setDuration(300)
					.withEndAction {
						bottomScrollBtnAnimation = null
					}
			}
		}

		fun setGone() {
			with(binding.chatBottomScrollBtnCv) {
				if (visibility != View.VISIBLE) return
				isGoneAnimatingBottomScrollBtn = true
				bottomScrollBtnAnimation?.cancel()
				bottomScrollBtnAnimation = animate()
					.scaleX(0f)
					.scaleY(0f)
					.alpha(0f)
					.setDuration(300)
					.withEndAction {
						visibility = View.GONE
						bottomScrollBtnAnimation = null
						isGoneAnimatingBottomScrollBtn = false
					}
			}
		}

		val isPossibleToShowBottomScrollBtn = linearLayoutManager.isOnListBottom().not()
						&& linearLayoutManager.isOnHigherPosition(BOTTOM_SCROLL_BTN_REFERENCE_POSITION)
						&& (channelViewModel.uiState.value.newChatNotice == null)
						&& channelViewModel.uiState.value.isNewerChatFullyLoaded

		if (isPossibleToShowBottomScrollBtn) {
			setVisible()
			return
		}

		if (linearLayoutManager.isOnListBottom()) setGone()
	}

	private fun initRcv() {
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				removeNewChatNoticeIfAppearsOnScreen()
				removeLastReadNoticeScrollFlagIfAppearsOnScreen()
				setBottomScrollBtnState()
				when {
					linearLayoutManager.isOnListTop() -> channelViewModel.onReachedTopChat()
					linearLayoutManager.isOnListBottom() -> channelViewModel.onReachedBottomChat()
				}
			}
		}

		binding.chattingRcv.apply {
			adapter = chatItemAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}

		binding.channelDrawerLayout.chatroomDrawerRcv.apply {
			adapter = channelDrawerAdapter
			layoutManager = LinearLayoutManager(this@ChannelActivity)
			setHasFixedSize(true)
		}
	}

	private fun removeNewChatNoticeIfAppearsOnScreen() {
		if (channelViewModel.uiState.value.newChatNotice == null) return
		val newestChatNotMineIndex = chatItemAdapter.newestChatNotMineIndex
		if (linearLayoutManager.isVisiblePosition(newestChatNotMineIndex).not()) return
		val item = chatItemAdapter.currentList[newestChatNotMineIndex] as ChatItem.Message
		channelViewModel.onReadNewestChatNotMineInList(item)
	}

	private fun removeLastReadNoticeScrollFlagIfAppearsOnScreen() {
		if (channelViewModel.uiState.value.needToScrollToLastReadChat.not()) return
		val lastReadChatNoticeIndex = chatItemAdapter.lastReadChatNoticeIndex
		if (linearLayoutManager.isVisiblePosition(lastReadChatNoticeIndex).not()) return
		channelViewModel.onReadLastReadNotice()
	}

	private fun moveToUserProfile(user: User) {
		val channelId = channelViewModel.uiState.value.channel.roomId
		val intent = Intent(this, UserProfileActivity::class.java)
			.putExtra(EXTRA_USER_ID, user.id)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private val channelSettingResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_CODE_USER_CHANNEL_EXIT) finish()
		}

	private fun moveChannelSetting() {
		val channelId = channelViewModel.uiState.value.channel.roomId
		val intent = Intent(this, ChannelSettingActivity::class.java)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		channelSettingResultLauncher.launch(intent)
	}

	private fun setExplodedChannelUiState(uiState: ChannelUiState) {
		if (uiState.channel.isExploded.not()) return
		showExplodedChannelNoticeDialog()
		with(binding.captureBtn) {
			isEnabled = false
			isClickable = false
			background = null
		}
		with(binding.chatInputEt) {
			isEnabled = false
			isClickable = false
			isFocusable = false
			setText("")
			setHint(R.string.unavailable_channel_edittext_hint)
		}
	}

	private fun setBannedClientUIState(uiState: ChannelUiState) {
		if (uiState.channel.isBanned.not()) return
		showBannedClientNoticeDialog()
		with(binding.captureBtn) {
			isEnabled = false
			isClickable = false
			background = null
		}
		with(binding.chatInputEt) {
			isEnabled = false
			isClickable = false
			isFocusable = false
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

	private fun showChannelExitWarningDialog(isClientHost: Boolean) {
		val existingFragment =
			supportFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_EXIT_WARNING)
		if (existingFragment != null) return
		val dialog = ChannelExitWarningDialog(
			isClientHost = isClientHost,
			onClickOkBtn = { channelViewModel.onClickChannelExitDialogBtn() }
		)
		dialog.show(supportFragmentManager, DIALOG_TAG_CHANNEL_EXIT_WARNING)
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			when {
				binding.drawerLayout.isDrawerOpen(GravityCompat.END) -> {
					binding.drawerLayout.closeDrawer(GravityCompat.END)
					return@addCallback
				}

				channelViewModel.uiState.value.isCaptureMode -> {
					channelViewModel.onClickCancelCapture()
					return@addCallback
				}
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

	private fun changeToCaptureMode() {
		with(binding) {
			chatInputEt.clearFocus()
			chatInputEt.isEnabled = false
			captureBtn.isEnabled = false
			chatSendBtn.isEnabled = false
			channelCaptureLayout.layout.visibility = View.VISIBLE
			captureModeBottomShadow.visibility = View.VISIBLE
		}
		chatItemAdapter.changeToCaptureMode()
	}

	private fun changeToDefaultMode() {
		with(binding) {
			chatInputEt.isEnabled = true
			captureBtn.isEnabled = true
			chatSendBtn.isEnabled = true
			channelCaptureLayout.layout.visibility = View.GONE
			captureModeBottomShadow.visibility = View.GONE
		}
		chatItemAdapter.changeToDefaultMode()
	}

	private fun makeCaptureImage(
		headerIndex: Int,
		bottomIndex: Int,
	) = lifecycleScope.launch {
		runCatching {
			binding.chattingRcv.captureItems(
				headerIndex = headerIndex,
				bottomIndex = bottomIndex
			)
		}.onSuccess { channelViewModel.onCompletedCapture() }
			.onFailure { channelViewModel.onFailedCapture() }
	}

	private fun moveToWholeText(chatId: Long) {
		val intent = Intent(this, ChatWholeTextActivity::class.java)
			.putExtra(ChatWholeTextViewmodel.EXTRA_CHAT_ID, chatId)
		startActivity(intent)
	}

	private fun copyTextToClipboard(text: String) {
		val clipData = ClipData.newPlainText(CLIPBOARD_LABEL, text)
		clipboardManager.setPrimaryClip(clipData)
		channelViewModel.onCopiedToClipboard()
	}

	private fun handleEvent(event: ChannelEvent) {
		when (event) {
			ChannelEvent.MoveBack -> finish()
			ChannelEvent.OpenOrCloseDrawer -> openOrCloseDrawer()
			ChannelEvent.ScrollToBottom -> scrollToBottom()
			ChannelEvent.MoveChannelSetting -> moveChannelSetting()
			is ChannelEvent.MoveUserProfile -> moveToUserProfile(event.user)
			is ChannelEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.bottomDivider
			)

			is ChannelEvent.NewChatOccurEvent -> checkIfNewChatNoticeIsRequired(event.chat)
			is ChannelEvent.ShowChannelExitWarningDialog ->
				showChannelExitWarningDialog(event.isClientHost)

			is ChannelEvent.MakeCaptureImage -> makeCaptureImage(
				headerIndex = event.headerIndex, bottomIndex = event.bottomIndex
			)

			is ChannelEvent.MoveToWholeText -> moveToWholeText(event.chatId)
			is ChannelEvent.CopyChatToClipboard -> copyTextToClipboard(event.message)
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
		internal const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		private const val CLIPBOARD_LABEL = "CLIPBOARD_LABEL"
		private const val DIALOG_TAG_CHANNEL_EXIT_WARNING = "DIALOG_TAG_CHANNEL_EXIT_WARNING"
		private const val DIALOG_TAG_EXPLODED_CHANNEL_NOTICE = "DIALOG_TAG_EXPLODED_CHANNEL_NOTICE"
		private const val DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE =
			"DIALOG_TAG_CHANNEL_BANNED_USER_NOTICE"
		private const val SOCKET_CONNECTION_SUCCESS_BAR_EXPOSURE_TIME = 1500L
		private const val BOTTOM_SCROLL_BTN_REFERENCE_POSITION = 3
	}
}