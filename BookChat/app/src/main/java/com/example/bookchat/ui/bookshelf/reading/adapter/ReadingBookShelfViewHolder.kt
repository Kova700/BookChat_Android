package com.example.bookchat.ui.bookshelf.reading.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookshelfDataBinding
import com.example.bookchat.databinding.ItemReadingBookshelfHeaderBinding
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordSwipeHelper
import com.example.bookchat.ui.bookshelf.reading.model.ReadingBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ReadingBookViewHolder(
	binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(readingBookShelfItem: ReadingBookShelfItem)
}

class ReadingBookHeaderViewHolder(
	val binding: ItemReadingBookshelfHeaderBinding,
) : ReadingBookViewHolder(binding) {
	override fun bind(readingBookShelfItem: ReadingBookShelfItem) {
		binding.totalItemCount = (readingBookShelfItem as ReadingBookShelfItem.Header).totalItemCount
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
		binding.swipeView.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
		binding.swipeView.setOnLongClickListener {
			onLongItemClickWithAnimation(binding.swipeView, onLongItemClick, bindingAdapterPosition)
			true
		}
		binding.pageBtn.setOnClickListener {
			onPageInputBtnClick?.invoke(bindingAdapterPosition)
		}
		binding.swipeBackground.setOnClickListener {
			onDeleteClick?.invoke(bindingAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(readingBookShelfItem: ReadingBookShelfItem) {
		val bookShelfListItem = (readingBookShelfItem as ReadingBookShelfItem.Item)
		binding.selectedBookTitleTv.text = bookShelfListItem.book.title
		binding.selectedBookAuthorsTv.text = bookShelfListItem.book.authorsString
		binding.readingPageTv.text =
			binding.root.context.getString(R.string.bookshelf_page_text, bookShelfListItem.pages)
		binding.bookImg.loadUrl(bookShelfListItem.book.bookCoverImageUrl)
		binding.selectedBookTitleTv.isSelected = true
		binding.selectedBookAuthorsTv.isSelected = true
		setViewHolderSwipeState(binding.swipeView, bookShelfListItem.isSwiped)
	}

	private fun setViewHolderSwipeState(swipeableView: View, isSwiped: Boolean) {
		if (isSwiped.not()) {
			swipeableView.translationX = 0f
			return
		}
		swipeableView.post {
			swipeableView.translationX =
				swipeableView.measuredWidth.toFloat() * AgonyRecordSwipeHelper.SWIPE_VIEW_PERCENT
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