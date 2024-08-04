package com.example.bookchat.ui.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.ui.home.model.HomeItem
import com.example.bookchat.utils.BookImgSizeManager
import javax.inject.Inject

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
			is HomeItem.BookHeader -> R.layout.item_home_book_header
			is HomeItem.BookItem -> R.layout.item_home_book
			is HomeItem.ChannelHeader -> R.layout.item_home_channel_header
			is HomeItem.ChannelItem -> R.layout.item_home_channel
			is HomeItem.BookDummy -> R.layout.item_home_book_dummy
			HomeItem.BookEmpty -> R.layout.item_home_book_empty
			HomeItem.ChannelEmpty -> R.layout.item_home_channel_empty
			HomeItem.BookLoading -> R.layout.layout_home_book_shimmer
			HomeItem.ChannelLoading -> R.layout.layout_home_channel_shimmer
			HomeItem.BookRetry -> R.layout.item_home_book_retry
			HomeItem.ChannelRetry -> R.layout.item_home_channel_retry
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