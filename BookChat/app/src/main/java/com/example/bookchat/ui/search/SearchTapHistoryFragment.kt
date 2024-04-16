package com.example.bookchat.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentSearchTapHistoryBinding
import com.example.bookchat.ui.search.adapter.searchhistory.SearchHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchTapHistoryFragment : Fragment() {

	private var _binding: FragmentSearchTapHistoryBinding? = null
	private val binding get() = _binding!!

	private val searchViewModel by viewModels<SearchViewModel>({ requireParentFragment() })

	@Inject
	lateinit var searchHistoryAdapter: SearchHistoryAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_search_tap_history, container, false
		)
		binding.viewmodel = searchViewModel
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
		searchViewModel.uiState.collect { state ->
			searchHistoryAdapter.submitList(state.searchHistory)
		}
	}

	private fun initAdapter() {
		searchHistoryAdapter.onItemClick = { position ->
			searchViewModel.onClickSearchHistory(position)
		}
		searchHistoryAdapter.onDeleteBtnClick = { position ->
			searchViewModel.onClickSearchHistoryDeleteBtn(position)
		}
	}

	private fun initRcv() {
		with(binding.historyRcv) {
			adapter = searchHistoryAdapter
			layoutManager = LinearLayoutManager(requireContext())
		}
	}
}