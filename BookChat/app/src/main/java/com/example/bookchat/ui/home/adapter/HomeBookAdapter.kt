package com.example.bookchat.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import javax.inject.Inject

class HomeBookAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<BookShelfListItem, HomeBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBookItemViewHolder {
		val binding: ItemWishBookshelfDataBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_data,
			parent, false
		)
		return HomeBookItemViewHolder(binding, bookImgSizeManager, onItemClick)
	}

	override fun onBindViewHolder(holder: HomeBookItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<BookShelfListItem>() {
			override fun areItemsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem.bookShelfId == newItem.bookShelfId

			override fun areContentsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem == newItem
		}
	}
}

class HomeBookItemViewHolder(
	private val binding: ItemWishBookshelfDataBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	fun bind(bookShelfListItem: BookShelfListItem) {
		binding.bookShelfListItem = bookShelfListItem
		binding.bookImg.loadUrl(bookShelfListItem.book.bookCoverImageUrl)
	}

}