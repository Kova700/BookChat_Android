package com.kova700.bookchat.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kova700.bookchat.feature.home.databinding.ItemHomeBookBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeBookDummyBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeBookEmptyBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeBookHeaderBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeBookRetryBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeChannelBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeChannelEmptyBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeChannelHeaderBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeChannelRetryBinding
import com.kova700.bookchat.feature.home.databinding.ItemHomeHeaderBinding
import com.kova700.bookchat.feature.home.databinding.LayoutHomeBookShimmerBinding
import com.kova700.bookchat.feature.home.databinding.LayoutHomeChannelShimmerBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.feature.home.R as homeR

fun getHomeItemViewHolder(
	parent: ViewGroup,
	itemViewType: Int,
	bookImgSizeManager: BookImgSizeManager,
	onClickBookItem: ((Int) -> Unit)? = null,
	onClickBookAddBtn: (() -> Unit)? = null,
	onClickRetryBookLoadBtn: (() -> Unit)? = null,
	onClickChannelItem: ((Int) -> Unit)? = null,
	onClickChannelAddBtn: (() -> Unit)? = null,
	onClickRetryChannelLoadBtn: (() -> Unit)? = null,
): HomeItemViewHolder {
	when (itemViewType) {
		homeR.layout.item_home_header -> {
			val binding = ItemHomeHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeHeaderViewHolder(binding)
		}

		homeR.layout.item_home_book_header -> {
			val binding = ItemHomeBookHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookHeaderViewHolder(binding)
		}

		homeR.layout.item_home_book -> {
			val binding = ItemHomeBookBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager,
				onClickBookItem = onClickBookItem
			)
		}

		homeR.layout.item_home_book_dummy -> {
			val binding = ItemHomeBookDummyBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookDummyViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager
			)
		}

		homeR.layout.item_home_book_empty -> {
			val binding = ItemHomeBookEmptyBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookEmptyViewHolder(
				binding = binding,
				onClickBookAddBtn = onClickBookAddBtn
			)
		}

		homeR.layout.layout_home_book_shimmer -> {
			val binding = LayoutHomeBookShimmerBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookShimmerViewHolder(
				binding = binding,
				bookImgSizeManager = bookImgSizeManager
			)
		}

		homeR.layout.item_home_book_retry -> {
			val binding = ItemHomeBookRetryBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookRetryViewHolder(
				binding = binding,
				onClickRetryBookLoadBtn = onClickRetryBookLoadBtn
			)
		}

		homeR.layout.item_home_channel_header -> {
			val binding = ItemHomeChannelHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelHeaderViewHolder(binding)
		}

		homeR.layout.item_home_channel -> {
			val binding = ItemHomeChannelBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelViewHolder(
				binding = binding,
				onClickChannelItem = onClickChannelItem
			)
		}

		homeR.layout.item_home_channel_empty -> {
			val binding = ItemHomeChannelEmptyBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelEmptyViewHolder(
				binding = binding,
				onClickChannelAddBtn = onClickChannelAddBtn
			)
		}

		homeR.layout.layout_home_channel_shimmer -> {
			val binding = LayoutHomeChannelShimmerBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelShimmerViewHolder(binding)
		}

		homeR.layout.item_home_channel_retry -> {
			val binding = ItemHomeChannelRetryBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelRetryViewHolder(
				binding = binding,
				onClickRetryChannelLoadBtn = onClickRetryChannelLoadBtn
			)
		}

		else -> throw Exception("unknown ViewHolder Type")
	}
}