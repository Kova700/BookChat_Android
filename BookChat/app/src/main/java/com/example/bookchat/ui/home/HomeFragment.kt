package com.example.bookchat.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment
import com.example.bookchat.ui.createchannel.MakeChannelActivity
import com.example.bookchat.ui.home.adapter.HomeItemAdapter
import com.example.bookchat.ui.home.model.HomeItem
import com.example.bookchat.utils.showSnackBar
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!
	private val homeViewModel: HomeViewModel by viewModels()

	@Inject
	lateinit var homeItemAdapter: HomeItemAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentHomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		initViewState()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.eventFlow.collect(::handleEvent)
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.uiState.collect { uiState ->
			homeItemAdapter.submitList(uiState.items)
			setViewState(uiState)
		}
	}

	private fun initViewState() {

	}

	private fun setViewState(uiState: HomeUiState) {
		binding.nicknameTv.text = getString(R.string.user_nickname, uiState.client.nickname)
	}

	private fun initAdapter() {
		homeItemAdapter.onClickBookItem = { itemPosition ->
			homeViewModel.onBookItemClick()
		}
		homeItemAdapter.onClickChannelItem = { itemPosition ->
			val item = homeItemAdapter.currentList[itemPosition] as HomeItem.ChannelItem
			homeViewModel.onChannelItemClick(item.roomId)
		}
		homeItemAdapter.onClickBookAddBtn = { homeViewModel.onClickMoveToSearch() }
		homeItemAdapter.onClickChannelAddBtn = { homeViewModel.onClickMakeChannel() }
		homeItemAdapter.onClickRetryBookLoadBtn = { homeViewModel.onClickRetryBookLoadBtn() }
		homeItemAdapter.onClickRetryChannelLoadBtn = { homeViewModel.onClickRetryChannelLoadBtn() }
	}

	private fun initRecyclerView() {
		val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
			.apply {
				justifyContent = JustifyContent.CENTER
				flexDirection = FlexDirection.ROW
				flexWrap = FlexWrap.WRAP
			}
		binding.homeItemRcv.adapter = homeItemAdapter
		binding.homeItemRcv.layoutManager = flexboxLayoutManager
	}

	private fun moveToReadingBookShelf() {
		(requireActivity() as MainActivity).navigateToBookShelfFragment()
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(requireContext(), ChannelActivity::class.java)
		intent.putExtra(ChannelListFragment.EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveToMakeChannel() {
		val intent = Intent(requireContext(), MakeChannelActivity::class.java)
		startActivity(intent)
	}

	private fun moveToSearch() {
		(requireActivity() as MainActivity).navigateToSearchFragment()
	}

	private fun handleEvent(event: HomeUiEvent) {
		when (event) {
			HomeUiEvent.MoveToMakeChannel -> moveToMakeChannel()
			HomeUiEvent.MoveToSearch -> moveToSearch()
			is HomeUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is HomeUiEvent.MoveToReadingBookShelf -> moveToReadingBookShelf()
			is HomeUiEvent.ShowSnackBar -> binding.root.showSnackBar(
				textId = event.stringId,
				anchor = binding.snackbarPoint
			)
		}
	}
}