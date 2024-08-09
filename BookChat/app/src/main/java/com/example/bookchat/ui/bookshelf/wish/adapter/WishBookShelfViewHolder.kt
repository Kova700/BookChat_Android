package com.example.bookchat.ui.bookshelf.wish.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchBookDummyBinding
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import com.example.bookchat.ui.bookshelf.wish.model.WishBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl

sealed class WishBookViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(wishBookShelfItem: WishBookShelfItem)
}

class WishBookHeaderViewHolder(
	val binding: ItemWishBookshelfHeaderBinding,
) : WishBookViewHolder(binding) {
	override fun bind(wishBookShelfItem: WishBookShelfItem) {
		val item = wishBookShelfItem as WishBookShelfItem.Header
		with(binding) {
			totalItemCountTv.text = root.context.getString(
				R.string.bookshelf_total_item_count_text,
				item.totalItemCount
			)
		}
	}
}

class WishBookItemViewHolder(
	private val binding: ItemWishBookshelfDataBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
) : WishBookViewHolder(binding) {

	init {
		with(binding) {
			root.setOnClickListener {
				onItemClick?.invoke(bindingAdapterPosition)
			}
			bookImgSizeManager.setBookImgSize(bookImg)
		}
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