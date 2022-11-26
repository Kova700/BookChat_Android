package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.SearchHistoryAdapter
import com.example.bookchat.databinding.FragmentSearchTapHistoryBinding
import com.example.bookchat.utils.DataStoreManager
import com.example.bookchat.viewmodel.SearchViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapHistoryFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapHistoryBinding
    private lateinit var searchHistoryAdapter :SearchHistoryAdapter
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_history, container, false)
        searchViewModel = ViewModelProvider(requireParentFragment(), ViewModelFactory()).get(SearchViewModel::class.java)
        binding.fragment = this
        initAdapter()
        initRcv()
        return binding.root
    }

    private fun initAdapter() = lifecycleScope.launch {
        val historyList = DataStoreManager.getSearchHistory() ?: mutableListOf()
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
        binding.historyRcv.adapter = searchHistoryAdapter
        binding.historyRcv.setHasFixedSize(true)
        binding.historyRcv.layoutManager = LinearLayoutManager(requireContext())
    }

    fun clearHistory() = lifecycleScope.launch {
        DataStoreManager.clearSearchHistory()
        searchHistoryAdapter.searchHistoryList = DataStoreManager.getSearchHistory() ?: mutableListOf()
        searchHistoryAdapter.notifyDataSetChanged()
    }

}