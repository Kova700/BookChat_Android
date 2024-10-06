package com.kova700.bookchat.feature.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kova700.bookchat.feature.search.R
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDummyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookHeaderBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookRetryBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBooksShimmerBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelHeaderBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelRetryBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelsShimmerBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchPagingRetryBinding
import com.kova700.bookchat.feature.search.databinding.LayoutSearchResultEmptyBinding
import com.kova700.bookchat.util.book.BookImgSizeManager

fun getSearchItemViewHolder(
	parent: ViewGroup,
	viewType: Int,
	bookImgSizeManager: BookImgSizeManager,
	onBookHeaderBtnClick: (() -> Unit)?,
	onBookItemClick: ((Int) -> Unit)?,
	onChannelHeaderBtnClick: (() -> Unit)?,
	onChannelItemClick: ((Int) -> Unit)?,
	onClickMakeChannelBtn: (() -> Unit)?,
	onBookRetryBtnClick: (() -> Unit)?,
	onChannelRetryBtnClick: (() -> Unit)?,
	onPagingRetryBtnClick: (() -> Unit)?,
): SearchItemViewHolder {

	return when (viewType) {
		R.layout.item_search_book_header -> {
			val binding = ItemSearchBookHeaderBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookHeaderViewHolder(
				binding = binding,
				onBookHeaderBtnClick = onBookHeaderBtnClick
			)
		}

		R.layout.item_search_book_data -> {
			val binding = ItemSearchBookDataBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookItemViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager,
				onItemClick = onBookItemClick
			)
		}

		R.layout.item_search_book_dummy -> {
			val binding = ItemSearchBookDummyBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookDummyViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager
			)
		}

		R.layout.item_search_book_empty -> {
			val binding = ItemSearchBookEmptyBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookEmptyViewHolder(binding)
		}

		R.layout.item_search_book_retry -> {
			val binding = ItemSearchBookRetryBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookRetryViewHolder(
				binding = binding,
				onRetryBtnClick = onBookRetryBtnClick
			)
		}

		R.layout.item_search_paging_retry -> {
			val binding = ItemSearchPagingRetryBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			PagingRetryViewHolder(
				binding = binding,
				onPagingRetryBtnClick = onPagingRetryBtnClick
			)
		}

		R.layout.item_search_books_shimmer -> {
			val binding = ItemSearchBooksShimmerBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BookLoadingViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager
			)
		}

		R.layout.item_search_channel_header -> {
			val binding = ItemSearchChannelHeaderBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			ChannelHeaderViewHolder(
				binding = binding,
				onChannelHeaderBtnClick = onChannelHeaderBtnClick
			)
		}

		R.layout.item_search_channel_data -> {
			val binding = ItemSearchChannelDataBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			ChannelItemViewHolder(
				binding = binding,
				onItemClick = onChannelItemClick
			)
		}

		R.layout.item_search_channel_empty -> {
			val binding = ItemSearchChannelEmptyBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			ChannelEmptyViewHolder(
				binding = binding,
				onClickMakeChannelBtn = onClickMakeChannelBtn
			)
		}

		R.layout.item_search_channel_retry -> {
			val binding = ItemSearchChannelRetryBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			ChannelRetryViewHolder(
				binding = binding,
				onRetryBtnClick = onChannelRetryBtnClick
			)
		}

		R.layout.item_search_channels_shimmer -> {
			val binding = ItemSearchChannelsShimmerBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			ChannelLoadingViewHolder(binding)
		}

		R.layout.layout_search_result_empty -> {
			val binding = LayoutSearchResultEmptyBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
			BothEmptyViewHolder(binding)
		}

		else -> throw Exception("unknown ViewHolder Type")
	}

}