package com.kova700.bookchat.feature.bookshelf.wish.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.databinding.ItemBookshelfPagingRetryBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookDummyBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookshelfDataBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemWishBookshelfHeaderBinding
import com.kova700.bookchat.feature.bookshelf.wish.model.WishBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.image.image.loadUrl

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
	private val binding: ItemWishBookDummyBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : WishBookViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.flexBoxDummyBookLayout)
	}

	override fun bind(wishBookShelfItem: WishBookShelfItem) {}
}

class WishPagingRetryViewHolder(
	private val binding: ItemBookshelfPagingRetryBinding,
	private val onClickPagingRetryBtn: (() -> Unit)?,
) : WishBookViewHolder(binding) {
	init {
		binding.retryBtn.setOnClickListener {
			onClickPagingRetryBtn?.invoke()
		}
	}

	override fun bind(wishBookShelfItem: WishBookShelfItem) {}
}