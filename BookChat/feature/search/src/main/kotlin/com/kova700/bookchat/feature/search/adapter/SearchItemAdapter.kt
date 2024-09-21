package com.kova700.bookchat.feature.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDummyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookHeaderBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelHeaderBinding
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.search.R as searchR

class SearchItemAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<SearchResultItem, SearchItemViewHolder>(SEARCH_ITEM_COMPARATOR) {

	var onBookHeaderBtnClick: (() -> Unit)? = null
	var onBookItemClick: ((Int) -> Unit)? = null
	var onChannelHeaderBtnClick: (() -> Unit)? = null
	var onChannelItemClick: ((Int) -> Unit)? = null
	var onClickMakeChannelBtn: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			SearchResultItem.BookHeader -> searchR.layout.item_search_book_header
			SearchResultItem.BookEmpty -> searchR.layout.item_search_book_empty
			is SearchResultItem.BookItem -> searchR.layout.item_search_book_data
			is SearchResultItem.BookDummy -> searchR.layout.item_search_book_dummy
			SearchResultItem.ChannelHeader -> searchR.layout.item_search_channel_header
			SearchResultItem.ChannelEmpty -> searchR.layout.item_search_channel_empty
			is SearchResultItem.ChannelItem -> searchR.layout.item_search_channel_data
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
		when (viewType) {
			searchR.layout.item_search_book_header -> {
				val binding = ItemSearchBookHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return BookHeaderViewHolder(binding, onBookHeaderBtnClick)
			}

			searchR.layout.item_search_book_empty -> {
				val binding = ItemSearchBookEmptyBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return BookEmptyViewHolder(binding)
			}

			searchR.layout.item_search_book_data -> {
				val binding = ItemSearchBookDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return BookItemViewHolder(binding, bookImgSizeManager, onBookItemClick)
			}

			searchR.layout.item_search_book_dummy -> {
				val binding = ItemSearchBookDummyBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return BookDummyViewHolder(binding, bookImgSizeManager)
			}

			searchR.layout.item_search_channel_header -> {
				val binding = ItemSearchChannelHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ChannelHeaderViewHolder(binding, onChannelHeaderBtnClick)
			}

			searchR.layout.item_search_channel_empty -> {
				val binding = ItemSearchChannelEmptyBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ChannelEmptyViewHolder(binding, onClickMakeChannelBtn)
			}

			searchR.layout.item_search_channel_data -> {
				val binding = ItemSearchChannelDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
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