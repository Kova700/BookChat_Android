package com.example.bookchat.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchTapResultBinding
import com.example.bookchat.ui.search.SearchUiState.SearchResultState
import com.example.bookchat.ui.search.adapter.SearchItemAdapter
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.utils.BookImgSizeManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
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
		savedInstanceState: Bundle?
	): View {
		_binding =
			DataBindingUtil.inflate(
				inflater, R.layout.fragment_search_tap_result, container, false
			)
		binding.viewmodel = searchViewModel
		binding.lifecycleOwner = this
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRcv()
		initViewState()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchViewModel.uiState.collect { uiState ->
			setViewVisibility(uiState)
			searchItemAdapter.submitList(uiState.searchResults)
		}
	}

	private fun initViewState() {
		initShimmerGridLayout()
		initShimmerBook()
	}

	private fun initShimmerGridLayout() {
		with(binding.resultShimmerLayout.shimmerBookRcvGridLayout) {
			columnCount = bookImgSizeManager.flexBoxBookSpanSize
			rowCount = 2
		}
	}

	private fun initShimmerBook() {
		with(binding.resultShimmerLayout) {
			bookImgSizeManager.setBookImgSize(shimmerBook1.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook2.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook3.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook4.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook5.bookImg)
			bookImgSizeManager.setBookImgSize(shimmerBook6.bookImg)
		}
	}

	private fun setViewVisibility(uiState: SearchUiState) {
		with(binding) {
			resultEmptyLayout.visibility =
				if (isResultBothEmpty(uiState)) View.VISIBLE else View.GONE
			resultShimmerLayout.shimmerLayout.visibility =
				if (isVisibleSkeleton(uiState)) View.VISIBLE else View.GONE
					.also { resultShimmerLayout.shimmerLayout.stopShimmer() }
			resultRcv.visibility =
				if (isVisibleRcv(uiState)) View.VISIBLE else View.INVISIBLE
		}
	}

	private fun isResultBothEmpty(uiState: SearchUiState) =
		(uiState.searchTapState is SearchUiState.SearchTapState.Result) &&
						(uiState.searchResultState == SearchResultState.Empty)

	private fun isVisibleSkeleton(uiState: SearchUiState) =
		(uiState.searchTapState is SearchUiState.SearchTapState.Result) &&
						(uiState.searchResultState == SearchResultState.Loading)

	private fun isVisibleRcv(uiState: SearchUiState) =
		(uiState.searchTapState is SearchUiState.SearchTapState.Result) &&
						((uiState.searchResultState == SearchResultState.Success) ||
										(uiState.searchResultState == SearchResultState.Error))

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
	}

	private fun initRcv() {
		val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
			.apply {
				justifyContent = JustifyContent.CENTER
				flexDirection = FlexDirection.ROW
				flexWrap = FlexWrap.WRAP
			}
		binding.resultRcv.adapter = searchItemAdapter
		binding.resultRcv.layoutManager = flexboxLayoutManager
	}

}