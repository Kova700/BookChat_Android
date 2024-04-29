package com.example.bookchat.ui.channel

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.User
import com.example.bookchat.ui.channel.adapter.ChatDataItemAdapter
import com.example.bookchat.ui.channel.adapter.chatdrawer.ChatRoomDrawerDataAdapter
import com.example.bookchat.ui.channel.adapter.chatdrawer.ChatRoomDrawerHeaderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelBinding
	private lateinit var chatDataItemAdapter: ChatDataItemAdapter
	private lateinit var chatRoomDrawerHeaderAdapter: ChatRoomDrawerHeaderAdapter
	private lateinit var chatRoomDrawerDataAdapter: ChatRoomDrawerDataAdapter
	private val channelViewModel by viewModels<ChannelViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_channel)
		with(binding) {
			lifecycleOwner = this@ChannelActivity
			viewmodel = channelViewModel
		}
		setBackPressedDispatcher()
		initAdapter()
		initRcv()
		observeUiState()
		observeEvent()
	}

	private fun initAdapter() {
		chatDataItemAdapter = ChatDataItemAdapter()
			.apply { registerAdapterDataObserver(adapterDataObserver) }
		chatRoomDrawerHeaderAdapter = ChatRoomDrawerHeaderAdapter()
		chatRoomDrawerDataAdapter = ChatRoomDrawerDataAdapter()
	}

	private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			onNewChatInsertedCallBack()
		}
	}

	private fun onNewChatInsertedCallBack() {
		if (channelViewModel.scrollForcedFlag) {
			scrollNewChatItem()
			channelViewModel.scrollForcedFlag = false
			return
		}

		if (channelViewModel.isFirstItemOnScreen) {
			scrollNewChatItem()
		}
	}

	private fun scrollNewChatItem() {
		binding.chattingRcv.scrollToPosition(0)
	}

	private fun initRcv() {
		val linearLayoutManager =
			LinearLayoutManager(this@ChannelActivity).apply { reverseLayout = true }

		// TODO : 스크롤 위로 올리면 아래로 스크롤 내릴 수 있는 버튼 보이기
		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				channelViewModel.loadNextChats(
					linearLayoutManager.findLastVisibleItemPosition()
				)
				val isFirstItemOnScreen = isFistItemOnScreen(recyclerView)
				channelViewModel.isFirstItemOnScreen = isFirstItemOnScreen
				if (isFirstItemOnScreen) {
					channelViewModel.newChatNoticeFlow.value = null
				}
			}
		}

		binding.chattingRcv.apply {
			adapter = chatDataItemAdapter
			setHasFixedSize(true)
			addItemDecoration(ChatItemDecoration())
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}

		binding.chatDrawerLayout.chatroomDrawerRcv.apply {
			val concatAdapterConfig =
				ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
			val concatAdapter = ConcatAdapter(
				concatAdapterConfig, chatRoomDrawerHeaderAdapter, chatRoomDrawerDataAdapter
			)
			adapter = concatAdapter
			layoutManager = LinearLayoutManager(this@ChannelActivity)
			setHasFixedSize(true)
		}
	}

	private fun isFistItemOnScreen(recyclerView: RecyclerView): Boolean {
		val lm = recyclerView.layoutManager as LinearLayoutManager
		val firstVisiblePosition: Int = lm.findFirstVisibleItemPosition()
		val lastVisiblePosition: Int = lm.findLastVisibleItemPosition()
		return 0 in firstVisiblePosition..lastVisiblePosition
	}

	private fun setBackPressedDispatcher() {
		onBackPressedDispatcher.addCallback {
			channelViewModel.onClickBackBtn()
		}
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelViewModel.uiStateFlow.collect { uiState ->
			chatDataItemAdapter.submitList(uiState.chats)
			uiState.channel?.let { updateDrawerHeader(it) }
			uiState.channel?.participants?.let { updateDrawerUserList(it) }
		}
	}

	private fun observeEvent() = lifecycleScope.launch {
		channelViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun updateDrawerHeader(channel: Channel) {
		chatRoomDrawerHeaderAdapter.channel = channel
		chatRoomDrawerHeaderAdapter.notifyItemChanged(0)
	}

	private fun updateDrawerUserList(users: List<User>) {
		chatRoomDrawerDataAdapter.submitList(users)
	}

	private fun handleEvent(event: ChannelEvent) {
		when (event) {
			ChannelEvent.MoveBack -> finish()
			ChannelEvent.CaptureChannel -> {}
			ChannelEvent.ScrollNewChannelItem -> scrollNewChatItem()
			ChannelEvent.OpenOrCloseDrawer -> openOrCloseDrawer()
		}
	}

	private fun openOrCloseDrawer() {
		val drawerLayout = binding.drawerLayout
		if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
			drawerLayout.closeDrawer(GravityCompat.END)
			return
		}
		drawerLayout.openDrawer(GravityCompat.END)
	}

	override fun onStop() {
		channelViewModel.saveTempSavedMessage()
		super.onStop()
	}
}