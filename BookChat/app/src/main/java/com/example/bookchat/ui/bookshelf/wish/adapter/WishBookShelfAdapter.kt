package com.example.bookchat.ui.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemFlexBoxDummyBinding
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import com.example.bookchat.ui.bookshelf.wish.WishBookShelfItem
import javax.inject.Inject

class WishBookShelfAdapter @Inject constructor() :
	ListAdapter<WishBookShelfItem, WishBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is WishBookShelfItem.Header -> R.layout.item_wish_bookshelf_header
			is WishBookShelfItem.Item -> R.layout.item_wish_bookshelf_data
			is WishBookShelfItem.Dummy -> R.layout.item_flex_box_dummy
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
				return WishBookItemViewHolder(binding, onItemClick)
			}

			else -> {
				val binding: ItemFlexBoxDummyBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_flex_box_dummy,
					parent, false
				)
				return WishBookDummyViewHolder(binding)
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
				newItem: WishBookShelfItem
			): Boolean {
				return oldItem.getCategoryId() == newItem.getCategoryId()
			}

			override fun areContentsTheSame(
				oldItem: WishBookShelfItem,
				newItem: WishBookShelfItem
			): Boolean {
				return when (oldItem) {
					is WishBookShelfItem.Header -> {
						newItem as WishBookShelfItem.Header
						oldItem == newItem
					}

					is WishBookShelfItem.Item -> {
						newItem as WishBookShelfItem.Item
						oldItem == newItem
					}

					is WishBookShelfItem.Dummy -> {
						newItem as WishBookShelfItem.Dummy
						oldItem == newItem
					}
				}

			}
		}
	}
}

sealed class WishBookViewHolder(
	binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(wishBookShelfItem: WishBookShelfItem)
}

class WishBookHeaderViewHolder(
	val binding: ItemWishBookshelfHeaderBinding,
) : WishBookViewHolder(binding) {
	override fun bind(wishBookShelfItem: WishBookShelfItem) {
		binding.totalItemCount = (wishBookShelfItem as WishBookShelfItem.Header).totalItemCount
	}
}

class WishBookItemViewHolder(
	private val binding: ItemWishBookshelfDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
) : WishBookViewHolder(binding) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	override fun bind(wishBookShelfItem: WishBookShelfItem) {
		binding.bookShelfListItem = (wishBookShelfItem as WishBookShelfItem.Item).bookShelfListItem
	}
}

class WishBookDummyViewHolder(
	val binding: ItemFlexBoxDummyBinding
) : WishBookViewHolder(binding) {
	override fun bind(wishBookShelfItem: WishBookShelfItem) {}
}