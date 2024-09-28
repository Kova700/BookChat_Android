package com.kova700.bookchat.feature.bookshelf.complete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.bookshelf.complete.model.CompleteBookShelfItem
import com.kova700.bookchat.feature.bookshelf.databinding.ItemBookshelfPagingRetryBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemCompleteBookshelfDataBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemCompleteBookshelfHeaderBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.bookshelf.R as bookshelfR

class CompleteBookShelfAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<CompleteBookShelfItem, CompleteBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null
	var onClickPagingRetryBtn: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is CompleteBookShelfItem.Header -> bookshelfR.layout.item_complete_bookshelf_header
			is CompleteBookShelfItem.Item -> bookshelfR.layout.item_complete_bookshelf_data
			CompleteBookShelfItem.PagingRetry -> bookshelfR.layout.item_bookshelf_paging_retry
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteBookViewHolder {
		when (viewType) {
			bookshelfR.layout.item_complete_bookshelf_header -> {
				val binding = ItemCompleteBookshelfHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return CompleteBookHeaderViewHolder(binding)
			}

			bookshelfR.layout.item_complete_bookshelf_data -> {
				val binding = ItemCompleteBookshelfDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return CompleteBookItemViewHolder(
					binding = binding,
					bookImgSizeManager = bookImgSizeManager,
					onItemClick = onItemClick,
					onLongItemClick = onLongItemClick,
					onDeleteClick = onDeleteClick
				)
			}

			bookshelfR.layout.item_bookshelf_paging_retry -> {
				val binding = ItemBookshelfPagingRetryBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return CompletePagingRetryViewHolder(
					binding = binding,
					onClickPagingRetryBtn = onClickPagingRetryBtn
				)
			}

			else -> throw IllegalArgumentException("Invalid viewType: $viewType")
		}
	}

	override fun onBindViewHolder(holder: CompleteBookViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<CompleteBookShelfItem>() {
			override fun areItemsTheSame(
				oldItem: CompleteBookShelfItem,
				newItem: CompleteBookShelfItem,
			): Boolean {
				return oldItem.getCategoryId() == newItem.getCategoryId()
			}

			override fun areContentsTheSame(
				oldItem: CompleteBookShelfItem,
				newItem: CompleteBookShelfItem,
			): Boolean {
				return oldItem == newItem
			}

		}
	}
}