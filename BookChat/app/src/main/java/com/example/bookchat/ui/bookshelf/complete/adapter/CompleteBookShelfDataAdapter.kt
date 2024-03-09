package com.example.bookchat.ui.bookshelf.complete.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookshelfDataBinding
import com.example.bookchat.ui.bookshelf.model.BookShelfListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompleteBookShelfDataAdapter @Inject constructor() :
	ListAdapter<BookShelfListItem, CompleteBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null
	var onLongItemClick: ((Int, Boolean) -> Unit)? = null
	var onDeleteClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteBookItemViewHolder {
		val binding: ItemCompleteBookshelfDataBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_complete_bookshelf_data,
			parent, false
		)
		return CompleteBookItemViewHolder(binding, onItemClick, onLongItemClick, onDeleteClick)
	}

	override fun onBindViewHolder(holder: CompleteBookItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	override fun getItemViewType(position: Int): Int = R.layout.item_complete_bookshelf_data

	companion object {
		val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<BookShelfListItem>() {
			override fun areItemsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem.bookShelfId == newItem.bookShelfId

			override fun areContentsTheSame(oldItem: BookShelfListItem, newItem: BookShelfListItem) =
				oldItem == newItem
		}
	}

}

class CompleteBookItemViewHolder(
	private val binding: ItemCompleteBookshelfDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
	private val onLongItemClick: ((Int, Boolean) -> Unit)?,
	private val onDeleteClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.swipeView.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
		binding.swipeView.setOnLongClickListener {
			onLongItemClickWithAnimation(binding.swipeView, onLongItemClick, bindingAdapterPosition)
			true
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
		swipeableView.translationX = swipeableView.width.toFloat() * SWIPE_VIEW_PERCENT
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