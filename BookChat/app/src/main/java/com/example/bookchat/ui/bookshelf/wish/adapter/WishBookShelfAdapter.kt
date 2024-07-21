package com.example.bookchat.ui.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchBookDummyBinding
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import javax.inject.Inject

class WishBookShelfAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<WishBookShelfItem, WishBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is WishBookShelfItem.Header -> R.layout.item_wish_bookshelf_header
			is WishBookShelfItem.Item -> R.layout.item_wish_bookshelf_data
			is WishBookShelfItem.Dummy -> R.layout.item_search_book_dummy
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishBookViewHolder {
		when (viewType) {
			R.layout.item_wish_bookshelf_header -> {
				val binding: ItemWishBookshelfHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_header,
					parent, false
				)
				return WishBookHeaderViewHolder(binding)
			}

			R.layout.item_wish_bookshelf_data -> {
				val binding: ItemWishBookshelfDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_data,
					parent, false
				)
				return WishBookItemViewHolder(binding, bookImgSizeManager, onItemClick)
			}

			else -> {
				val binding: ItemSearchBookDummyBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_search_book_dummy,
					parent, false
				)
				return WishBookDummyViewHolder(binding, bookImgSizeManager)
			}
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