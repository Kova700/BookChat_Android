package com.example.bookchat.ui.channel.chatting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
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
import com.example.bookchat.ui.channel.chatting.adapter.ChatItemAdapter
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.ui.channel.drawer.adapter.ChannelDrawerAdapter
import com.example.bookchat.ui.channel.drawer.dialog.ChannelExitWarningDialog
import com.example.bookchat.ui.channel.drawer.mapper.toUser
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem
import com.example.bookchat.ui.channel.userprofile.UserProfileActivity
import com.example.bookchat.utils.isOnListBottom
import com.example.bookchat.utils.isOnListTop
import com.example.bookchat.utils.isVisiblePosition
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
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
			addTextChangedListener { text ->
				val message = text?.toString() ?: return@addTextChangedListener
				channelViewModel.onChangeEnteredMessage(message)
			}
		}
		with(binding.chatDrawerLayout.channelSettingBtn) {
			visibility = if (channelViewModel.uiState.value.isClientHost) View.VISIBLE else View.GONE
		}

	}

	private fun setViewState(uiState: ChannelUiState) {
		setMessageBarState(uiState)
		setNewChatNoticeState(uiState)
		setSocketConnectionUiState(uiState)
	}

	private fun setSocketConnectionUiState(uiState: ChannelUiState) {
		//TODO : 이 상태에 맞게 유저에게 소켓 연결 상태 UI 제공 (상단에 소켓연결상태)
		//  연결되면 카톡처럼 상단에 "연결되었습니다" 3초정도 보여주고 사라짐(GONE)
		//  연결 끊기면 "오프라인 상태입니다" 회색 배경 계속 보여주기
		uiState.socketState
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

	private fun initLayoutManager() {
		linearLayoutManager = LinearLayoutManager(this)
		linearLayoutManager.reverseLayout = true
	}

	private fun initAdapter() {
		chatItemAdapter.onClickUserProfile = { itemPosition ->
			val user = (chatItemAdapter.currentList[itemPosition] as ChatItem.AnotherUser).sender!!
			channelViewModel.onClickUserProfile(user)
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
		val channelId = channelViewModel.uiState.value.channel?.roomId ?: return
		val intent = Intent(this, UserProfileActivity::class.java)
			.putExtra(EXTRA_USER_ID, user.id)
			.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveChannelSetting() {
//		val intent = Intent(this, Activity::class.java)
//		startActivity(intent)
	}

	private fun setExplodedChannelUiState() {
		//TODO : "방장이 채팅방을 종료했습니다. 더 이상 대화가 불가능합니다." Notice Dialog
		//  isExplode로 변경된 채팅방을 emit받은 ChannelActivity에서는 채팅 입력을 불가능하게 비활성화 UI를 노출
	}

	private fun setBannedClientUIState() {
		//TODO : "채팅방 관리자에의해 강퇴되었습니다." Notice Dialog
		//  isBanned로 변경된 채팅방을 emit받은 ChannelActivity에서는 채팅 입력을 불가능하게 비활성화 UI를 노출
	}

	private fun showChannelExitWarningDialog(clientAuthority: ChannelMemberAuthority) {
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

			ChannelEvent.ChannelExplode -> setExplodedChannelUiState()
			ChannelEvent.ClientBanned -> setBannedClientUIState()
		}
	}

	private fun openOrCloseDrawer() {
		with(binding.drawerLayout) {
			if (isDrawerOpen(GravityCompat.END)) {
				closeDrawer(GravityCompat.END)
				return
			}
			openDrawer(GravityCompat.END)
		}
	}

	companion object {
		const val EXTRA_USER_ID = "EXTRA_USER_ID"
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		const val DIALOG_TAG_CHANNEL_EXIT_WARNING = "DIALOG_TAG_CHANNEL_EXIT_WARNING"
	}
}