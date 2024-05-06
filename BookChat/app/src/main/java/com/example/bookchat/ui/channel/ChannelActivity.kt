package com.example.bookchat.ui.channel

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivityChannelBinding
import com.example.bookchat.ui.channel.adapter.chat.ChatDataItemAdapter
import com.example.bookchat.ui.channel.adapter.drawer.ChannelDrawerAdapter
import com.example.bookchat.utils.makeToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

	private lateinit var binding: ActivityChannelBinding
	private val channelViewModel by viewModels<ChannelViewModel>()

	@Inject
	lateinit var chatDataItemAdapter: ChatDataItemAdapter

	@Inject
	lateinit var channelDrawerAdapter: ChannelDrawerAdapter

	@Inject
	lateinit var chatItemDecoration: ChatItemDecoration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_channel)
		binding.lifecycleOwner = this@ChannelActivity
		binding.viewmodel = channelViewModel
		setBackPressedDispatcher()
		initAdapter()
		initRcv()
		observeUiState()
		observeEvent()
	}

	override fun onStop() {
		channelViewModel.saveTempSavedMessage()
		super.onStop()
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelViewModel.uiStateFlow.collect { uiState ->
			chatDataItemAdapter.submitList(uiState.chats)
			channelDrawerAdapter.submitList(uiState.drawerItems)
		}
	}

	private fun observeEvent() = lifecycleScope.launch {
		channelViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initAdapter() {
		chatDataItemAdapter.registerAdapterDataObserver(adapterDataObserver)
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

	private fun handleEvent(event: ChannelEvent) {
		when (event) {
			ChannelEvent.MoveBack -> finish()
			ChannelEvent.CaptureChannel -> {}
			ChannelEvent.ScrollNewChannelItem -> scrollNewChatItem()
			ChannelEvent.OpenOrCloseDrawer -> openOrCloseDrawer()
			is ChannelEvent.MakeToast -> makeToast(event.stringId)
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
}