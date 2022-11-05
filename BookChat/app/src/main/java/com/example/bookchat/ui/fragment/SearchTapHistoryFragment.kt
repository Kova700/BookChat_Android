package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.SearchHistoryAdapter
import com.example.bookchat.databinding.FragmentSearchTapHistoryBinding
import com.example.bookchat.utils.DataStoreManager
import kotlinx.coroutines.launch

/*추후 확장이 필요하다 느끼면 ViewModel로 로직 이동*/
class SearchTapHistoryFragment : Fragment() {
    private lateinit var binding : FragmentSearchTapHistoryBinding
    private lateinit var searchHistoryAdapter :SearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //최근 검색어 받아와야함
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_tap_history, container, false)
        binding.fragment = this
        initAdapter()
        initRcv()
        return binding.root
    }

    private fun initAdapter() = lifecycleScope.launch {
        val historyList = DataStoreManager.getSearchHistory() ?: mutableListOf()
        searchHistoryAdapter = SearchHistoryAdapter(historyList)
        searchHistoryAdapter.setItemClickListener(object :
            SearchHistoryAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                //해당 검색어로 검색
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