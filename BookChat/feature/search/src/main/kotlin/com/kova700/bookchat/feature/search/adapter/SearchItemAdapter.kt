package com.kova700.bookchat.feature.search.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
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
	var onBookRetryBtnClick: (() -> Unit)? = null
	var onChannelRetryBtnClick: (() -> Unit)? = null
	var onPagingRetryBtnClick: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			SearchResultItem.BookHeader -> searchR.layout.item_search_book_header
			is SearchResultItem.BookItem -> searchR.layout.item_search_book_data
			is SearchResultItem.BookDummy -> searchR.layout.item_search_book_dummy
			SearchResultItem.BookEmpty -> searchR.layout.item_search_book_empty
			SearchResultItem.BookRetry -> searchR.layout.item_search_book_retry
			SearchResultItem.BookLoading -> searchR.layout.item_search_books_shimmer
			SearchResultItem.ChannelHeader -> searchR.layout.item_search_channel_header
			is SearchResultItem.ChannelItem -> searchR.layout.item_search_channel_data
			SearchResultItem.ChannelEmpty -> searchR.layout.item_search_channel_empty
			SearchResultItem.ChannelRetry -> searchR.layout.item_search_channel_retry
			SearchResultItem.ChannelLoading -> searchR.layout.item_search_channels_shimmer
			SearchResultItem.BothEmpty -> searchR.layout.layout_search_result_empty
			SearchResultItem.PagingRetry -> searchR.layout.item_search_paging_retry
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemViewHolder {
		return getSearchItemViewHolder(
			parent = parent,
			viewType = viewType,
			bookImgSizeManager = bookImgSizeManager,
			onBookHeaderBtnClick = onBookHeaderBtnClick,
			onBookItemClick = onBookItemClick,
			onChannelHeaderBtnClick = onChannelHeaderBtnClick,
			onChannelItemClick = onChannelItemClick,
			onClickMakeChannelBtn = onClickMakeChannelBtn,
			onBookRetryBtnClick = onBookRetryBtnClick,
			onPagingRetryBtnClick = onPagingRetryBtnClick,
			onChannelRetryBtnClick = onChannelRetryBtnClick,
		)
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