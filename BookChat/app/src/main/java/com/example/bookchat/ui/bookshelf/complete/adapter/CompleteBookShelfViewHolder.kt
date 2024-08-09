package com.example.bookchat.ui.bookshelf.complete.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookshelfDataBinding
import com.example.bookchat.databinding.ItemCompleteBookshelfHeaderBinding
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordSwipeHelper
import com.example.bookchat.ui.bookshelf.complete.model.CompleteBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class CompleteBookViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(completeBookShelfItem: CompleteBookShelfItem)
}

class CompleteBookHeaderViewHolder(
	val binding: ItemCompleteBookshelfHeaderBinding,
) : CompleteBookViewHolder(binding) {
	override fun bind(completeBookShelfItem: CompleteBookShelfItem) {
		val item = completeBookShelfItem as CompleteBookShelfItem.Header
		with(binding) {
			totalItemCountTv.text =
				root.context.getString(
					R.string.bookshelf_total_item_count_text,
					item.totalItemCount
				)
		}
	}
}

class CompleteBookItemViewHolder(
	private val binding: ItemCompleteBookshelfDataBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
	private val onLongItemClick: ((Int, Boolean) -> Unit)?,
	private val onDeleteClick: ((Int) -> Unit)?,
) : CompleteBookViewHolder(binding) {

	init {
		with(binding) {
			swipeView.setOnClickListener {
				onItemClick?.invoke(bindingAdapterPosition)
			}
			swipeView.setOnLongClickListener {
				onLongItemClickWithAnimation(swipeView, onLongItemClick, bindingAdapterPosition)
				true
			}
			swipeBackground.root.setOnClickListener {
				onDeleteClick?.invoke(bindingAdapterPosition)
			}
			bookImgSizeManager.setBookImgSize(bookImg)
		}
	}

	override fun bind(completeBookShelfItem: CompleteBookShelfItem) {
		val bookShelfListItem = (completeBookShelfItem as CompleteBookShelfItem.Item)
		with(binding) {
			selectedBookTitleTv.text = bookShelfListItem.book.title
			selectedBookAuthorsTv.text = bookShelfListItem.book.authorsString
			starRating.rating = bookShelfListItem.star?.value ?: 0F
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