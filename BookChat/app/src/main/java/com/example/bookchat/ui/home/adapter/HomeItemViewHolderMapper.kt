package com.example.bookchat.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemHomeBookBinding
import com.example.bookchat.databinding.ItemHomeBookDummyBinding
import com.example.bookchat.databinding.ItemHomeBookEmptyBinding
import com.example.bookchat.databinding.ItemHomeBookHeaderBinding
import com.example.bookchat.databinding.ItemHomeBookRetryBinding
import com.example.bookchat.databinding.ItemHomeChannelBinding
import com.example.bookchat.databinding.ItemHomeChannelEmptyBinding
import com.example.bookchat.databinding.ItemHomeChannelHeaderBinding
import com.example.bookchat.databinding.ItemHomeChannelRetryBinding
import com.example.bookchat.databinding.ItemHomeHeaderBinding
import com.example.bookchat.databinding.LayoutHomeBookShimmerBinding
import com.example.bookchat.databinding.LayoutHomeChannelShimmerBinding
import com.example.bookchat.utils.BookImgSizeManager

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
		R.layout.item_home_header -> {
			val binding = ItemHomeHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeHeaderViewHolder(binding)
		}

		R.layout.item_home_book_header -> {
			val binding = ItemHomeBookHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeBookHeaderViewHolder(binding)
		}

		R.layout.item_home_book -> {
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

		R.layout.item_home_book_dummy -> {
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

		R.layout.item_home_book_empty -> {
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

		R.layout.layout_home_book_shimmer -> {
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

		R.layout.item_home_book_retry -> {
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

		R.layout.item_home_channel_header -> {
			val binding = ItemHomeChannelHeaderBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelHeaderViewHolder(binding)
		}

		R.layout.item_home_channel -> {
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

		R.layout.item_home_channel_empty -> {
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

		R.layout.layout_home_channel_shimmer -> {
			val binding = LayoutHomeChannelShimmerBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			return HomeChannelShimmerViewHolder(binding)
		}

		R.layout.item_home_channel_retry -> {
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