package com.example.bookchat.ui.channelList

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentChannelListBinding
import com.example.bookchat.domain.model.NetworkState
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.adpater.ChannelListAdapter
import com.example.bookchat.ui.channelList.dialog.ChannelSettingDialog
import com.example.bookchat.ui.channelList.model.ChannelListItem
import com.example.bookchat.ui.createchannel.MakeChannelActivity
import com.example.bookchat.utils.makeToast
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

	@Inject
	lateinit var channelListIItemSwipeHelper: ChannelListIItemSwipeHelper

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
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
		channelListViewModel.uiState.collect { uiState ->
			channelListAdapter.submitList(uiState.channelListItem)
			setViewState(uiState)
		}
	}

	private fun setViewState(uiState: ChannelListUiState) {
		setNetworkStateBarUiState(uiState)
	}

	private fun setNetworkStateBarUiState(uiState: ChannelListUiState) {
		when (uiState.networkState) {
			NetworkState.CONNECTED -> {
				binding.networkStateBar.visibility = View.GONE
			}

			NetworkState.DISCONNECTED -> {
				binding.networkStateBar.setText(R.string.please_connect_the_network)
				binding.networkStateBar.setBackgroundColor(Color.parseColor("#666666"))
				binding.networkStateBar.visibility = View.VISIBLE
			}
		}
	}

	private fun initAdapter() {
		channelListAdapter.onSwipe = { position, isSwiped ->
			val item = channelListAdapter.currentList[position] as ChannelListItem.ChannelItem
			channelListViewModel.onSwipeChannelItem(item, isSwiped)
		}
		channelListAdapter.onClick = { position ->
			val item = (channelListAdapter.currentList[position] as ChannelListItem.ChannelItem)
			channelListViewModel.onClickChannelItem(item.roomId)
		}
		channelListAdapter.onLongClick = { position ->
			val item = (channelListAdapter.currentList[position] as ChannelListItem.ChannelItem)
			channelListViewModel.onLongClickChannelItem(item)
		}
		channelListAdapter.onClickMuteRelatedBtn = { position ->
			val item = channelListAdapter.currentList[position] as ChannelListItem.ChannelItem
			channelListViewModel.onClickMuteRelatedBtn(item)
		}
		channelListAdapter.onClickTopPinRelatedBtn = { position ->
			val item = channelListAdapter.currentList[position] as ChannelListItem.ChannelItem
			channelListViewModel.onClickTopPinRelatedBtn(item)
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
			initSwipeHelper(this)
			addOnScrollListener(rcvScrollListener)
		}
	}

	private fun initSwipeHelper(recyclerView: RecyclerView) {
		val itemTouchHelper = ItemTouchHelper(channelListIItemSwipeHelper)
		itemTouchHelper.attachToRecyclerView(recyclerView)
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

	private fun showChannelSettingDialog(channel: ChannelListItem.ChannelItem) {
		val existingFragment =
			childFragmentManager.findFragmentByTag(DIALOG_TAG_CHANNEL_SETTING)
		if (existingFragment != null) return

		val dialog = ChannelSettingDialog(
			channel = channel,
			onClickOkExitBtn = { channelListViewModel.onClickChannelExit(channel.roomId) },
			onClickMuteRelatedBtn = { channelListViewModel.onClickMuteRelatedBtn(channel) },
			onClickTopPinRelatedBtn = { channelListViewModel.onClickTopPinRelatedBtn(channel) },
		)
		dialog.show(childFragmentManager, DIALOG_TAG_CHANNEL_SETTING)
	}

	private fun handleEvent(event: ChannelListUiEvent) {
		when (event) {
			is ChannelListUiEvent.MoveToMakeChannelPage -> moveToMakeChannel()
			is ChannelListUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is ChannelListUiEvent.MoveToSearchChannelPage -> moveToSearchChannelPage()
			is ChannelListUiEvent.ShowChannelSettingDialog -> showChannelSettingDialog(event.channel)
			is ChannelListUiEvent.MakeToast -> makeToast(event.stringId)
		}
	}

	companion object {
		const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
		private const val DIALOG_TAG_CHANNEL_SETTING = "DIALOG_TAG_CHANNEL_SETTING"
	}
}