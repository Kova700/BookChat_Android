package com.example.bookchat.ui.bookshelf.wish.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemSearchBookDummyBinding
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl

sealed class WishBookViewHolder(
	binding: ViewDataBinding,
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
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
) : WishBookViewHolder(binding) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(wishBookShelfItem: WishBookShelfItem) {
		val bookShelfListItem = (wishBookShelfItem as WishBookShelfItem.Item)
		binding.bookImg.loadUrl(bookShelfListItem.book.bookCoverImageUrl)
	}
}

class WishBookDummyViewHolder(
	private val binding: ItemSearchBookDummyBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : WishBookViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.flexBoxDummyBookLayout)
	}

	override fun bind(wishBookShelfItem: WishBookShelfItem) {}
}