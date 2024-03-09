package com.example.bookchat.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.ui.search.adapter.searchhistory.SearchHistoryAdapter
import com.example.bookchat.databinding.FragmentSearchTapHistoryBinding
import com.example.bookchat.utils.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
@AndroidEntryPoint
class SearchTapHistoryFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapHistoryBinding
    private lateinit var searchHistoryAdapter : SearchHistoryAdapter
    private val searchViewModel: SearchViewModel by viewModels( { requireParentFragment() } )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_history, container, false)
        binding.fragment = this
        roadHistory()
        return binding.root
    }

    private fun roadHistory() = lifecycleScope.launch{
        val historyList = DataStoreManager.getSearchHistory() ?: mutableListOf()
        initAdapter(historyList)
        initRcv()
    }

    private fun initAdapter(historyList :MutableList<String>) {
        searchHistoryAdapter = SearchHistoryAdapter(historyList)
        searchHistoryAdapter.setHasStableIds(true)
        searchHistoryAdapter.setItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {
            override fun onClick(v: View) {
                val keyword = (v as TextView).text.toString()
                searchViewModel.clickHistory(keyword)
            }
        })
    }

    private fun initRcv() {
        with(binding){
            historyRcv.adapter = searchHistoryAdapter
            historyRcv.setHasFixedSize(true)
            historyRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    fun clearHistory() = lifecycleScope.launch {
        DataStoreManager.clearSearchHistory()
        searchHistoryAdapter.searchHistoryList = DataStoreManager.getSearchHistory() ?: mutableListOf()
        searchHistoryAdapter.notifyDataSetChanged()
    }

}