package com.example.bookchat.ui.bookshelf.complete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookshelfDataBinding
import com.example.bookchat.databinding.ItemCompleteBookshelfHeaderBinding
import com.example.bookchat.ui.bookshelf.complete.model.CompleteBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import javax.inject.Inject

class CompleteBookShelfAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<CompleteBookShelfItem, CompleteBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is CompleteBookShelfItem.Header -> R.layout.item_complete_bookshelf_header
			is CompleteBookShelfItem.Item -> R.layout.item_complete_bookshelf_data
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteBookViewHolder {
		when (viewType) {
			R.layout.item_complete_bookshelf_header -> {
				val binding = ItemCompleteBookshelfHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return CompleteBookHeaderViewHolder(binding)
			}

			else -> {
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