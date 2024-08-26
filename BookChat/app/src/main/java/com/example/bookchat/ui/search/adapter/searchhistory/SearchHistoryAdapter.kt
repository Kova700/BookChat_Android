package com.example.bookchat.ui.search.adapter.searchhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemSearchHistoryBinding
import javax.inject.Inject

class SearchHistoryAdapter @Inject constructor() :
	ListAdapter<String, SearchHistoryViewHolder>(SEARCH_HISTORY_COMPARATOR) {

	var onItemClick: ((Int) -> Unit)? = null
	var onDeleteBtnClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
		val binding = ItemSearchHistoryBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)
		return SearchHistoryViewHolder(binding, onItemClick, onDeleteBtnClick)
	}

	override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val SEARCH_HISTORY_COMPARATOR = object : DiffUtil.ItemCallback<String>() {
			override fun areItemsTheSame(oldItem: String, newItem: String) =
				oldItem == newItem

			override fun areContentsTheSame(oldItem: String, newItem: String) =
				oldItem == newItem
		}
	}
}

class SearchHistoryViewHolder(
	private val binding: ItemSearchHistoryBinding,
	private val onItemClick: ((Int) -> Unit)?,
	private val onDeleteBtnClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {
	init {
		binding.searchHistoryTv.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}

		binding.deleteSearchHistoryBtn.setOnClickListener {
			onDeleteBtnClick?.invoke(absoluteAdapterPosition)
		}
	}

	fun bind(historyKeyword: String) {
		binding.searchHistoryTv.text = historyKeyword
	}
}