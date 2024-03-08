package com.example.bookchat.ui.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import javax.inject.Inject

class WishBookShelfDataAdapter @Inject constructor() :
	ListAdapter<BookShelfListItem, WishBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishBookItemViewHolder {
		val binding: ItemWishBookshelfDataBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_data,
			parent, false
		)
		return WishBookItemViewHolder(binding, onItemClick)
	}

	override fun onBindViewHolder(holder: WishBookItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_data

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<BookShelfListItem>() {
			override fun areItemsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem.bookShelfId == newItem.bookShelfId

			override fun areContentsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem == newItem
		}
	}
}

class WishBookItemViewHolder(
	private val binding: ItemWishBookshelfDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	fun bind(bookShelfListItem: BookShelfListItem) {
		binding.bookShelfListItem = bookShelfListItem
	}
}