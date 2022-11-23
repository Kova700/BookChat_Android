package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchHistoryBinding
import com.example.bookchat.utils.DataStoreManager
import kotlinx.coroutines.runBlocking

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
        holder.bind(searchHistoryList[holder.itemId.toInt()])

        binding.searchHistoryTv.setOnClickListener {
            itemClickListener.onClick(it, holder.itemId.toInt())
        }

        binding.deleteSearchHistoryBtn.setOnClickListener {
            searchHistoryList.removeAt(holder.itemId.toInt())
            overWriteHistory(searchHistoryList)
            this@SearchHistoryAdapter.notifyDataSetChanged()
        }
    }

    private fun overWriteHistory(historyList: List<String>) = runBlocking {
        DataStoreManager.overWriteHistory(historyList)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
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