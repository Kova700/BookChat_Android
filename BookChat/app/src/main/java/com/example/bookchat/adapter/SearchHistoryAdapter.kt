package com.example.bookchat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchHistoryBinding
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.DataStoreManager
import kotlinx.coroutines.runBlocking

//리스트 어댑터로 수정 예정
class SearchHistoryAdapter(var searchHistoryList: MutableList<String>) :
    RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder>() {

    private lateinit var binding: ItemSearchHistoryBinding
    private lateinit var itemClickListener: OnItemClickListener

    inner class SearchHistoryViewHolder(val binding: ItemSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind(historyKeyword : String){
            binding.historyKeyword = historyKeyword
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_search_history, parent, false)
        return SearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        holder.bind(searchHistoryList[position])
        binding.searchHistoryTv.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
        binding.deleteSearchHistoryBtn.setOnClickListener {
            searchHistoryList.removeAt(position)
            overWriteHistory(searchHistoryList)
            this.notifyDataSetChanged()
        }
    }

    private fun overWriteHistory(historyList: List<String>) = runBlocking {
        DataStoreManager.overWriteHistory(historyList)
    }


    override fun getItemCount(): Int {
        return searchHistoryList.size
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
}