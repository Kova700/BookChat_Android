package com.kova700.bookchat.feature.channellist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.core.navigation.ChannelNavigator
import com.kova700.bookchat.core.navigation.MainNavigationViewModel
import com.kova700.bookchat.core.navigation.MainRoute
import com.kova700.bookchat.core.navigation.MakeChannelActivityNavigator
import com.kova700.bookchat.feature.channel.drawer.dialog.ChannelExitWarningDialog
import com.kova700.bookchat.feature.channellist.adpater.ChannelListAdapter
import com.kova700.bookchat.feature.channellist.databinding.FragmentChannelListBinding
import com.kova700.bookchat.feature.channellist.dialog.ChannelSettingDialog
import com.kova700.bookchat.feature.channellist.model.ChannelListItem
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChannelListFragment : Fragment() {

	private var _binding: FragmentChannelListBinding? = null
	private val binding get() = _binding!!
	private val channelListViewModel by activityViewModels<ChannelListViewModel>()
	private val mainNavigationViewmodel by activityViewModels<MainNavigationViewModel>()

	@Inject
	lateinit var channelListAdapter: ChannelListAdapter

	@Inject
	lateinit var channelListIItemSwipeHelper: ChannelListIItemSwipeHelper

	@Inject
	lateinit var channelNavigator: ChannelNavigator

	@Inject
	lateinit var makeChannelNavigator: MakeChannelActivityNavigator

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentChannelListBinding.inflate(inflater, container, false)
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
		channelListViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		channelListViewModel.uiState.collect { uiState ->
			channelListAdapter.submitList(uiState.channelListItem)
			setViewState(uiState)
		}
	}

	private fun initViewState() {
		with(binding) {
			channelAddBtn.setOnClickListener { channelListViewModel.onClickPlusBtn() }
			emptyChannelLayout.makeChannelBtn.setOnClickListener { channelListViewModel.onClickPlusBtn() }
			channelSearchBtn.setOnClickListener { channelListViewModel.onClickChannelSearchBtn() }
		}
	}

	private fun setViewState(uiState: ChannelListUiState) {
		setViewVisibility(uiState)
		binding.retryChannelLayout.retryBtn.setOnClickListener { channelListViewModel.onClickInitRetry() }
	}

	private fun setViewVisibility(uiState: ChannelListUiState) {
		with(binding) {
			emptyChannelLayout.root.visibility =
				if (uiState.isEmpty) View.VISIBLE else View.GONE
			channelListRcv.visibility =
				if (uiState.isNotEmpty) View.VISIBLE else View.GONE
			retryChannelLayout.root.visibility =
				if (uiState.isInitError) View.VISIBLE else View.GONE
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
		channelListAdapter.onClickRetryBtn = { channelListViewModel.onClickPagingRetry() }
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

		with(binding.channelListRcv) {
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

	private fun moveToSearchChannelPage() {
		mainNavigationViewmodel.navigateTo(MainRoute.Search)
	}

	private fun showChannelSettingDialog(
		isClientHost: Boolean,
		channel: ChannelListItem.ChannelItem,
	) {
		val existingFragment =
			childFragmentManager.findFragmentByTag(ChannelSettingDialog.TAG)
		if (existingFragment != null) return

		val dialog = ChannelSettingDialog(
			channel = channel,
			onClickOkExitBtn = { showChannelExitWarningDialog(isClientHost, channel) },
			onClickMuteRelatedBtn = { channelListViewModel.onClickMuteRelatedBtn(channel) },
			onClickTopPinRelatedBtn = { channelListViewModel.onClickTopPinRelatedBtn(channel) },
		)
		dialog.show(childFragmentManager, ChannelSettingDialog.TAG)
	}

	private fun showChannelExitWarningDialog(
		isClientHost: Boolean,
		channel: ChannelListItem.ChannelItem,
	) {
		val existingFragment =
			childFragmentManager.findFragmentByTag(ChannelExitWarningDialog.TAG)
		if (existingFragment != null) return

		val dialog = ChannelExitWarningDialog(
			isClientHost = isClientHost,
			onClickOkBtn = { channelListViewModel.onClickChannelExit(channel.roomId) }
		)
		dialog.show(childFragmentManager, ChannelExitWarningDialog.TAG)
	}

	private fun handleEvent(event: ChannelListUiEvent) {
		when (event) {
			is ChannelListUiEvent.MoveToMakeChannelPage -> moveToMakeChannel()
			is ChannelListUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is ChannelListUiEvent.MoveToSearchChannelPage -> moveToSearchChannelPage()
			is ChannelListUiEvent.ShowChannelSettingDialog -> showChannelSettingDialog(
				isClientHost = event.isClientHost,
				channel = event.channel
			)

			is ChannelListUiEvent.ShowSnackBar -> binding.root.showSnackBar(event.stringId)
		}
	}
}