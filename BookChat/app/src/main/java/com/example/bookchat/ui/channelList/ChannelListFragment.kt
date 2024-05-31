package com.example.bookchat.ui.channelList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentChannelListBinding
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.adpater.ChannelListAdapter
import com.example.bookchat.ui.channelList.model.ChannelListItem
import com.example.bookchat.ui.createchannel.MakeChannelActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : Fragment() {

	private var _binding: FragmentChannelListBinding? = null
	private val binding get() = _binding!!
	private val channelListViewModel by activityViewModels<ChannelListViewModel>()

	@Inject
	lateinit var channelListAdapter: ChannelListAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_channel_list, container, false
		)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = channelListViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		channelListViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		channelListViewModel.uiStateFlow.collect { uiState ->
			channelListAdapter.submitList(uiState.channelListItem)
		}
	}

	private fun initAdapter() {
		//TODO : long Click :채팅방 상단고정, 알림 끄기 설정 가능한 다이얼로그
		//TODO : Swipe : 상단고정, 알림 끄기 UI 노출
		channelListAdapter.onItemClick = { itemPosition ->
			channelListViewModel.onChannelItemClick(
				(channelListAdapter.currentList[itemPosition] as ChannelListItem.ChannelItem).roomId
			)
		}
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

		with(binding.chatRcv) {
			adapter = channelListAdapter
			setHasFixedSize(true)
			layoutManager = linearLayoutManager
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(requireContext(), ChannelActivity::class.java)
		intent.putExtra(EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveToMakeChannel() {
		val intent = Intent(requireContext(), MakeChannelActivity::class.java)
		startActivity(intent)
	}

	private fun moveToSearchChannelPage() {
		//TODO : 검색 Fragment로 이동
	}

	private fun handleEvent(event: ChannelListUiEvent) {
		when (event) {
			is ChannelListUiEvent.MoveToMakeChannelPage -> moveToMakeChannel()
			is ChannelListUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is ChannelListUiEvent.MoveToSearchChannelPage -> moveToSearchChannelPage()
		}
	}

	companion object {
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
	}
}