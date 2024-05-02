package com.example.bookchat.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchBookDataBinding
import com.example.bookchat.databinding.ItemSearchBookDummyBinding
import com.example.bookchat.databinding.ItemSearchBookEmptyBinding
import com.example.bookchat.databinding.ItemSearchBookHeaderBinding
import com.example.bookchat.databinding.ItemSearchChannelDataBinding
import com.example.bookchat.databinding.ItemSearchChannelEmptyBinding
import com.example.bookchat.databinding.ItemSearchChannelHeaderBinding
import com.example.bookchat.ui.search.model.SearchResultItem
import javax.inject.Inject

class SearchItemAdapter @Inject constructor() :
	ListAdapter<SearchResultItem, SearchItemViewHolder>(SEARCH_ITEM_COMPARATOR) {

	var onBookHeaderBtnClick: (() -> Unit)? = null
	var onBookItemClick: ((Int) -> Unit)? = null
	var onChannelHeaderBtnClick: (() -> Unit)? = null
	var onChannelItemClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			SearchResultItem.BookHeader -> R.layout.item_search_book_header
			SearchResultItem.BookEmpty -> R.layout.item_search_book_empty
			is SearchResultItem.BookItem -> R.layout.item_search_book_data
			is SearchResultItem.BookDummy -> R.layout.item_search_book_dummy
			SearchResultItem.ChannelHeader -> R.layout.item_search_channel_header
			SearchResultItem.ChannelEmpty -> R.layout.item_search_channel_empty
			is SearchResultItem.ChannelItem -> R.layout.item_search_channel_data
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
		when (viewType) {
			R.layout.item_search_book_header -> {
				val binding: ItemSearchBookHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_book_header,
					parent, false
				)
				return BookHeaderViewHolder(binding, onBookHeaderBtnClick)
			}

			R.layout.item_search_book_empty -> {
				val binding: ItemSearchBookEmptyBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_book_empty,
					parent, false
				)
				return BookEmptyViewHolder(binding)
			}

			R.layout.item_search_book_data -> {
				val binding: ItemSearchBookDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_book_data,
					parent, false
				)
				return BookItemViewHolder(binding, onBookItemClick)
			}

			R.layout.item_search_book_dummy -> {
				val binding: ItemSearchBookDummyBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_book_dummy,
					parent, false
				)
				return BookDummyViewHolder(binding)
			}

			R.layout.item_search_channel_header -> {
				val binding: ItemSearchChannelHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_channel_header,
					parent, false
				)
				return ChannelHeaderViewHolder(binding, onChannelHeaderBtnClick)
			}

			R.layout.item_search_channel_empty -> {
				val binding: ItemSearchChannelEmptyBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_channel_empty,
					parent, false
				)
				return ChannelEmptyViewHolder(binding)
			}

			R.layout.item_search_channel_data -> {
				val binding: ItemSearchChannelDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_channel_data,
					parent, false
				)
				return ChannelItemViewHolder(binding, onChannelItemClick)
			}

			else -> throw Exception("unknown ViewHolder Type")
		}
	}

	override fun onBindViewHolder(holder: SearchItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val SEARCH_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<SearchResultItem>() {
			override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem) =
				oldItem == newItem
		}
	}
}