package com.kova700.bookchat.feature.bookshelf.reading.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.bookshelf.databinding.ItemReadingBookshelfDataBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemReadingBookshelfHeaderBinding
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.bookshelf.R as bookshelfR

class ReadingBookShelfDataAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<ReadingBookShelfItem, ReadingBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {

	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onPageInputBtnClick: ((Int) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ReadingBookShelfItem.Header -> bookshelfR.layout.item_reading_bookshelf_header
			is ReadingBookShelfItem.Item -> bookshelfR.layout.item_reading_bookshelf_data
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ReadingBookViewHolder {

		when (viewType) {
			bookshelfR.layout.item_reading_bookshelf_header -> {
				val binding = ItemReadingBookshelfHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ReadingBookHeaderViewHolder(binding)
			}

			else -> {
				val binding = ItemReadingBookshelfDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ReadingBookItemViewHolder(
					binding = binding,
					bookImgSizeManager = bookImgSizeManager,
					onItemClick = onItemClick,
					onLongItemClick = onLongItemClick,
					onPageInputBtnClick = onPageInputBtnClick,
					onDeleteClick = onDeleteClick
				)
			}
		}

	}

	override fun onBindViewHolder(holder: ReadingBookViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ReadingBookShelfItem>() {
			override fun areItemsTheSame(
				oldItem: ReadingBookShelfItem,
				newItem: ReadingBookShelfItem,
			): Boolean {
				return oldItem.getCategoryId() == newItem.getCategoryId()
			}

			override fun areContentsTheSame(
				oldItem: ReadingBookShelfItem,
				newItem: ReadingBookShelfItem,
			): Boolean {
				return oldItem == newItem
			}
		}
	}
}