package com.example.bookchat.ui.bookshelf.reading.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookshelfDataBinding
import com.example.bookchat.databinding.ItemReadingBookshelfHeaderBinding
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordSwipeHelper
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReadingBookShelfDataAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<ReadingBookShelfItem, ReadingBookViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {

	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onPageInputBtnClick: ((Int) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ReadingBookShelfItem.Header -> R.layout.item_reading_bookshelf_header
			is ReadingBookShelfItem.Item -> R.layout.item_reading_bookshelf_data
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ReadingBookViewHolder {

		when (viewType) {
			R.layout.item_reading_bookshelf_header -> {
				val binding: ItemReadingBookshelfHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_reading_bookshelf_header,
					parent, false
				)
				return ReadingBookHeaderViewHolder(binding)
			}

			else -> {
				val binding: ItemReadingBookshelfDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_reading_bookshelf_data,
					parent, false
				)
				return ReadingBookItemViewHolder(
					binding,
					bookImgSizeManager,
					onItemClick,
					onLongItemClick,
					onPageInputBtnClick,
					onDeleteClick
				)
			}
		}

	}

	override fun onBindViewHolder(holder: ReadingBookViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ReadingBookShelfItem>() {
			override fun areItemsTheSame(
				oldItem: ReadingBookShelfItem,
				newItem: ReadingBookShelfItem,
			): Boolean {
				return oldItem.getCategoryId() == newItem.getCategoryId()
			}

			override fun areContentsTheSame(
				oldItem: ReadingBookShelfItem,
				newItem: ReadingBookShelfItem,
			): Boolean {
				return oldItem == newItem
			}
		}
	}
}

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
		val bookShelfListItem = (readingBookShelfItem as ReadingBookShelfItem.Item).bookShelfListItem
		binding.bookShelfListItem = bookShelfListItem
		binding.bookImg.loadUrl(bookShelfListItem.book.bookCoverImageUrl)
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