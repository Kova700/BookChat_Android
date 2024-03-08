package com.example.bookchat.ui.bookshelf.reading.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookshelfDataBinding
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import com.example.bookchat.ui.bookshelf.wish.adapter.WishBookShelfDataAdapter.Companion.BOOK_SHELF_ITEM_COMPARATOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReadingBookShelfDataAdapter @Inject constructor() :
	ListAdapter<BookShelfListItem, ReadingBookShelfDataViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {

	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onPageInputBtnClick: ((Int) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ReadingBookShelfDataViewHolder {
		val binding: ItemReadingBookshelfDataBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_reading_bookshelf_data,
			parent, false
		)
		return ReadingBookShelfDataViewHolder(
			binding,
			onItemClick,
			onLongItemClick,
			onPageInputBtnClick,
			onDeleteClick
		)
	}

	override fun onBindViewHolder(holder: ReadingBookShelfDataViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	override fun getItemViewType(position: Int): Int = R.layout.item_reading_bookshelf_data
}

class ReadingBookShelfDataViewHolder(
	private val binding: ItemReadingBookshelfDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
	private val onLongItemClick: ((Int, Boolean) -> Unit)?,
	private val onPageInputBtnClick: ((Int) -> Unit)?,
	private val onDeleteClick: ((Int) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

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
	}

	fun bind(bookShelfListItem: BookShelfListItem) {
		binding.bookShelfListItem = bookShelfListItem
		setViewHolderSwipeState(binding.swipeView, bookShelfListItem.isSwiped)
	}

	private fun setViewHolderSwipeState(swipeableView: View, isSwiped: Boolean) {
		if (isSwiped.not()) {
			swipeableView.translationX = 0f
			return
		}
		swipeableView.translationX =
			swipeableView.width.toFloat() * SWIPE_VIEW_PERCENT
	}

	private fun onLongItemClickWithAnimation(
		swipeableView: View,
		onLongItemClick: ((Int, Boolean) -> Unit)?,
		bindingAdapterPosition: Int
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