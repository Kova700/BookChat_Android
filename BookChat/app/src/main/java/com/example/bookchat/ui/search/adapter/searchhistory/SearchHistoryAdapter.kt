package com.example.bookchat.ui.search.adapter.searchhistory

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
            this.binding.searchHistoryTv.setOnClickListener {
                itemClickListener.onClick(it)
            }
            this.binding.deleteSearchHistoryBtn.setOnClickListener{
                searchHistoryList.removeAt(this.absoluteAdapterPosition)
                overWriteHistory(searchHistoryList)
                this@SearchHistoryAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_search_history, parent, false)
        return SearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        holder.bind(searchHistoryList[position])
    }

    private fun overWriteHistory(historyList: List<String>) = runBlocking {
        DataStoreManager.overWriteHistory(historyList)
    }

    //notifyDataSetChanged()시에 수정된 ItemView구분을 위해 사용 (수정된 ItemView만 갱신)
    //ListAdapter와 DiffUtil사용하면 굳이 사용하지 않아도 됌
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return searchHistoryList.size
    }

    interface OnItemClickListener {
        fun onClick(v: View)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
}