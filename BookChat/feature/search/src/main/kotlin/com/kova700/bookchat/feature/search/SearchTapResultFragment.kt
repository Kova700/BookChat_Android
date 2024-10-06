package com.kova700.bookchat.feature.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.kova700.bookchat.feature.search.adapter.SearchItemAdapter
import com.kova700.bookchat.feature.search.databinding.FragmentSearchTapResultBinding
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapResultFragment : Fragment() {

	private var _binding: FragmentSearchTapResultBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var searchItemAdapter: SearchItemAdapter

	private val searchViewModel by activityViewModels<SearchViewModel>()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = FragmentSearchTapResultBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRcv()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.uiState.collect { uiState ->
			searchItemAdapter.submitList(uiState.searchResults)
		}
	}


	private fun initAdapter() {
		searchItemAdapter.onBookHeaderBtnClick = {
			searchViewModel.onBookHeaderBtnClick()
		}
		searchItemAdapter.onBookItemClick = { position ->
			searchViewModel.onBookItemClick(
				((searchItemAdapter.currentList[position]) as SearchResultItem.BookItem)
			)
		}
		searchItemAdapter.onChannelHeaderBtnClick = {
			searchViewModel.onChannelHeaderBtnClick()
		}
		searchItemAdapter.onChannelItemClick = { position ->
			searchViewModel.onChannelItemClick(
				((searchItemAdapter.currentList[position]) as SearchResultItem.ChannelItem).roomId
			)
		}
		searchItemAdapter.onClickMakeChannelBtn = {
			searchViewModel.onClickMakeChannelBtn()
		}
		searchItemAdapter.onBookRetryBtnClick = {
			searchViewModel.onClickBookRetryBtn()
		}
		searchItemAdapter.onChannelRetryBtnClick = {
			searchViewModel.onClickChannelRetryBtn()
		}
	}

	private fun initRcv() {
		val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
			.apply {
				justifyContent = JustifyContent.CENTER
				flexDirection = FlexDirection.ROW
				flexWrap = FlexWrap.WRAP
			}
		with(binding) {
			resultRcv.adapter = searchItemAdapter
			resultRcv.layoutManager = flexboxLayoutManager
		}
	}

}