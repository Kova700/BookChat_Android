package com.kova700.bookchat.feature.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.feature.home.model.HomeItem
import javax.inject.Inject
import com.kova700.bookchat.feature.home.R as homeR

class HomeItemAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<HomeItem, HomeItemViewHolder>(HOME_ITEM_COMPARATOR) {
	var onClickBookItem: ((Int) -> Unit)? = null
	var onClickBookAddBtn: (() -> Unit)? = null
	var onClickRetryBookLoadBtn: (() -> Unit)? = null
	var onClickChannelItem: ((Int) -> Unit)? = null
	var onClickChannelAddBtn: (() -> Unit)? = null
	var onClickRetryChannelLoadBtn: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is HomeItem.Header -> homeR.layout.item_home_header
			is HomeItem.BookHeader -> homeR.layout.item_home_book_header
			is HomeItem.BookItem -> homeR.layout.item_home_book
			is HomeItem.ChannelHeader -> homeR.layout.item_home_channel_header
			is HomeItem.ChannelItem -> homeR.layout.item_home_channel
			is HomeItem.BookDummy -> homeR.layout.item_home_book_dummy
			HomeItem.BookEmpty -> homeR.layout.item_home_book_empty
			HomeItem.ChannelEmpty -> homeR.layout.item_home_channel_empty
			HomeItem.BookLoading -> homeR.layout.layout_home_book_shimmer
			HomeItem.ChannelLoading -> homeR.layout.layout_home_channel_shimmer
			HomeItem.BookRetry -> homeR.layout.item_home_book_retry
			HomeItem.ChannelRetry -> homeR.layout.item_home_channel_retry
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
		return getHomeItemViewHolder(
			parent = parent,
			itemViewType = viewType,
			bookImgSizeManager = bookImgSizeManager,
			onClickBookItem = onClickBookItem,
			onClickBookAddBtn = onClickBookAddBtn,
			onClickRetryBookLoadBtn = onClickRetryBookLoadBtn,
			onClickChannelItem = onClickChannelItem,
			onClickChannelAddBtn = onClickChannelAddBtn,
			onClickRetryChannelLoadBtn = onClickRetryChannelLoadBtn,
		)
	}

	override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val HOME_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<HomeItem>() {
			override fun areItemsTheSame(oldItem: HomeItem, newItem: HomeItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: HomeItem, newItem: HomeItem) =
				oldItem == newItem
		}
	}
}