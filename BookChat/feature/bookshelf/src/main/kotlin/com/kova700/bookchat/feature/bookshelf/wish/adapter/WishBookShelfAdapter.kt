package com.kova700.bookchat.feature.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.bookshelf.databinding.ItemBookshelfPagingRetryBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookDummyBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookshelfDataBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookshelfHeaderBinding
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.bookshelf.R as bookshelfR

class WishBookShelfAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<WishBookShelfItem, WishBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onClickItem: ((Int) -> Unit)? = null
	var onClickPagingRetryBtn: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is WishBookShelfItem.Header -> bookshelfR.layout.item_wish_bookshelf_header
			is WishBookShelfItem.Item -> bookshelfR.layout.item_wish_bookshelf_data
			is WishBookShelfItem.Dummy -> bookshelfR.layout.item_wish_book_dummy
			is WishBookShelfItem.PagingRetry -> bookshelfR.layout.item_bookshelf_paging_retry
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishBookViewHolder {
		when (viewType) {
			bookshelfR.layout.item_wish_bookshelf_header -> {
				val binding = ItemWishBookshelfHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return WishBookHeaderViewHolder(binding)
			}

			bookshelfR.layout.item_wish_bookshelf_data -> {
				val binding = ItemWishBookshelfDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return WishBookItemViewHolder(
					binding = binding,
					bookImgSizeManager = bookImgSizeManager,
					onItemClick = onClickItem
				)
			}

			bookshelfR.layout.item_wish_book_dummy -> {
				val binding = ItemWishBookDummyBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return WishBookDummyViewHolder(
					binding = binding,
					bookImgSizeManager = bookImgSizeManager
				)
			}

			bookshelfR.layout.item_bookshelf_paging_retry -> {
				val binding = ItemBookshelfPagingRetryBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return WishPagingRetryViewHolder(
					binding = binding,
					onClickPagingRetryBtn = onClickPagingRetryBtn
				)
			}

			else -> throw IllegalArgumentException("Invalid viewType: $viewType")
		}

	}

	override fun onBindViewHolder(holder: WishBookViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<WishBookShelfItem>() {
			override fun areItemsTheSame(
				oldItem: WishBookShelfItem,
				newItem: WishBookShelfItem,
			): Boolean {
				return oldItem.getCategoryId() == newItem.getCategoryId()
			}

			override fun areContentsTheSame(
				oldItem: WishBookShelfItem,
				newItem: WishBookShelfItem,
			): Boolean {
				return oldItem == newItem
			}
		}
	}
}