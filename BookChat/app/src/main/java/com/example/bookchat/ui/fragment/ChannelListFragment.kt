package com.example.bookchat.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentChatRoomListBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.activity.ChannelActivity
import com.example.bookchat.ui.activity.MakeChatRoomActivity
import com.example.bookchat.ui.adapter.userchatroomlist.ChannelListDataAdapter
import com.example.bookchat.ui.adapter.userchatroomlist.ChannelListHeaderAdapter
import com.example.bookchat.ui.viewmodel.ChannelListViewModel
import com.example.bookchat.ui.viewmodel.contract.ChannelListUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelListFragment : Fragment() {

	private lateinit var binding: FragmentChatRoomListBinding
	private lateinit var channelListHeaderAdapter: ChannelListHeaderAdapter
	private lateinit var channelListDataAdapter: ChannelListDataAdapter
	private val channelListViewModel: ChannelListViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding =
			DataBindingUtil.inflate(inflater, R.layout.fragment_chat_room_list, container, false)
		binding.viewmodel = channelListViewModel
		initAdapter()
		initRecyclerView()
		observeUiState()
		observeUiEvent()

		return binding.root
	}

	private fun observeUiState() = lifecycleScope.launch {
		channelListViewModel.uiStateFlow.collect { uiState ->
			channelListDataAdapter.submitList(uiState.channels)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		channelListViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initAdapter() {
		//TODO : long Click Listener(채팅방 상단고정, 알림 끄기 설정 가능한 다이얼로그 띄우기)
		//TODO : Swipe (상단고정, 알림 끄기 UI 보이기)
		val chatRoomItemClickListener = object : ChannelListDataAdapter.OnItemClickListener {
			override fun onItemClick(channel: Channel) {
				val intent = Intent(requireContext(), ChannelActivity::class.java)
				intent.putExtra(EXTRA_CHAT_ROOM_ID, channel.roomId)
				startActivity(intent)
			}
		}
		channelListHeaderAdapter = ChannelListHeaderAdapter()
		channelListDataAdapter = ChannelListDataAdapter()
		channelListDataAdapter.setItemClickListener(chatRoomItemClickListener)
	}

	private fun initRecyclerView() {
		val linearLayoutManager = LinearLayoutManager(requireContext())

		val rcvScrollListener = object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)
				channelListViewModel.loadNextChannels(
					linearLayoutManager.findLastVisibleItemPosition()
				)
			}
		}

		with(binding) {
			val concatAdapterConfig =
				ConcatAdapter.Config.Builder().apply { setIsolateViewTypes(false) }.build()
			val concatAdapter = ConcatAdapter(
				concatAdapterConfig,
				channelListHeaderAdapter,
				channelListDataAdapter
			)
			chatRcv.apply {
				adapter = concatAdapter
				setHasFixedSize(true)
				layoutManager = linearLayoutManager
				addOnScrollListener(rcvScrollListener)
			}
		}
	}

	private fun handleEvent(event: ChannelListUiEvent) = when (event) {
		ChannelListUiEvent.MoveToMakeChannelPage -> {
			val intent = Intent(requireContext(), MakeChatRoomActivity::class.java)
			startActivity(intent)
		}

		ChannelListUiEvent.MoveToSearchChannelPage -> {}
	}

	companion object {
		const val EXTRA_CHAT_ROOM_ID = "EXTRA_CHAT_ROOM_LIST_ITEM"
	}
}