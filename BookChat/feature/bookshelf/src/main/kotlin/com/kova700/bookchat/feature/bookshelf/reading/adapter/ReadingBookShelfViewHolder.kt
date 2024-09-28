package com.kova700.bookchat.feature.bookshelf.reading.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.bookshelf.databinding.ItemBookshelfPagingRetryBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemReadingBookshelfDataBinding
import com.kova700.bookchat.feature.bookshelf.databinding.ItemReadingBookshelfHeaderBinding
import com.kova700.bookchat.feature.bookshelf.reading.model.ReadingBookShelfItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.image.image.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ReadingBookViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(readingBookShelfItem: ReadingBookShelfItem)
}

class ReadingBookHeaderViewHolder(
	val binding: ItemReadingBookshelfHeaderBinding,
) : ReadingBookViewHolder(binding) {
	override fun bind(readingBookShelfItem: ReadingBookShelfItem) {
		val item = readingBookShelfItem as ReadingBookShelfItem.Header
		with(binding) {
			totalItemCountTv.text =
				root.context.getString(
					R.string.bookshelf_total_item_count_text,
					item.totalItemCount
				)
		}
	}
}

class ReadingBookItemViewHolder(
	private val binding: ItemReadingBookshelfDataBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
	private val onLongItemClick: ((Int, Boolean) -> Unit)?,
	private val onPageInputBtnClick: ((Int) -> Unit)?,
	private val onDeleteClick: ((Int) -> Unit)?,
) : ReadingBookViewHolder(binding) {

	init {
		with(binding) {
			swipeView.setOnClickListener {
				onItemClick?.invoke(bindingAdapterPosition)
			}
			swipeView.setOnLongClickListener {
				onLongItemClickWithAnimation(swipeView, onLongItemClick, bindingAdapterPosition)
				true
			}
			pageBtn.setOnClickListener {
				onPageInputBtnClick?.invoke(bindingAdapterPosition)
			}
			swipeBackground.root.setOnClickListener {
				onDeleteClick?.invoke(bindingAdapterPosition)
			}
			bookImgSizeManager.setBookImgSize(bookImg)
		}
	}

	override fun bind(readingBookShelfItem: ReadingBookShelfItem) {
		val bookShelfListItem = (readingBookShelfItem as ReadingBookShelfItem.Item)
		with(binding) {
			selectedBookTitleTv.text = bookShelfListItem.book.title
			selectedBookAuthorsTv.text = bookShelfListItem.book.authorsString
			readingPageTv.text =
				binding.root.context.getString(R.string.bookshelf_page_text, bookShelfListItem.pages)
			bookImg.loadUrl(bookShelfListItem.book.bookCoverImageUrl)
			selectedBookTitleTv.isSelected = true
			selectedBookAuthorsTv.isSelected = true
			setViewHolderSwipeState(swipeView, bookShelfListItem.isSwiped)
		}
	}

	private fun setViewHolderSwipeState(swipeableView: View, isSwiped: Boolean) {
		if (isSwiped.not()) {
			swipeableView.translationX = 0f
			return
		}
		swipeableView.post {
			swipeableView.translationX =
				swipeableView.measuredWidth.toFloat() * SWIPE_VIEW_PERCENT
		}
	}

	private fun onLongItemClickWithAnimation(
		swipeableView: View,
		onLongItemClick: ((Int, Boolean) -> Unit)?,
		bindingAdapterPosition: Int,
	) = CoroutineScope(Dispatchers.Main).launch {
		val swipedX = swipeableView.width.toFloat() * SWIPE_VIEW_PERCENT

		if (swipeableView.translationX != 0F) {
			while (swipeableView.translationX > 0F) {
				swipeableView.translationX -= swipedX / 20
				delay(5L)
			}
			swipeableView.translationX = 0F
			onLongItemClick?.invoke(bindingAdapterPosition, false)
			return@launch
		}

		while (swipeableView.translationX < swipedX) {
			swipeableView.translationX += swipedX / 20
			delay(5L)
		}
		swipeableView.translationX = swipedX
		onLongItemClick?.invoke(bindingAdapterPosition, true)
	}

	companion object {
		private const val SWIPE_VIEW_PERCENT = 0.3F
	}
}

class ReadingPagingRetryViewHolder(
	private val binding: ItemBookshelfPagingRetryBinding,
	private val onClickPagingRetryBtn: (() -> Unit)?,
) : ReadingBookViewHolder(binding) {
	init {
		binding.retryBtn.setOnClickListener {
			onClickPagingRetryBtn?.invoke()
		}
	}

	override fun bind(readingBookShelfItem: ReadingBookShelfItem) {}
}