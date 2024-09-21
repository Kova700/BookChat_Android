package com.kova700.bookchat.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kova700.bookchat.core.navigation.ChannelNavigator
import com.kova700.bookchat.core.navigation.MainNavigationViewModel
import com.kova700.bookchat.core.navigation.MainRoute
import com.kova700.bookchat.core.navigation.MakeChannelActivityNavigator
import com.kova700.bookchat.feature.home.adapter.HomeItemAdapter
import com.kova700.bookchat.feature.home.databinding.FragmentHomeBinding
import com.kova700.bookchat.feature.home.model.HomeItem
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!
	private val homeViewModel: HomeViewModel by viewModels()
	private val mainNavigationViewmodel by activityViewModels<MainNavigationViewModel>()

	@Inject
	lateinit var homeItemAdapter: HomeItemAdapter

	@Inject
	lateinit var channelNavigator: ChannelNavigator

	@Inject
	lateinit var makeChannelNavigator: MakeChannelActivityNavigator

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
		}
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
		mainNavigationViewmodel.navigateTo(MainRoute.Bookshelf)
	}

	private fun moveToSearch() {
		mainNavigationViewmodel.navigateTo(MainRoute.Search)
	}

	private fun moveToChannel(channelId: Long) {
		channelNavigator.navigate(
			currentActivity = requireActivity(),
			channelId = channelId,
		)
	}

	private fun moveToMakeChannel() {
		makeChannelNavigator.navigate(
			currentActivity = requireActivity(),
		)
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