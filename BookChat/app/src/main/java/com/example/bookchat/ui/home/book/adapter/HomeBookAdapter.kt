package com.example.bookchat.ui.home.book.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemHomeBookBinding
import com.example.bookchat.ui.home.book.model.HomeBookItem
import com.example.bookchat.utils.BookImgSizeManager
import javax.inject.Inject

class HomeBookAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<HomeBookItem, HomeBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeBookItemViewHolder {
		val binding: ItemHomeBookBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_home_book,
			parent, false
		)
		return HomeBookItemViewHolder(binding, bookImgSizeManager, onItemClick)
	}

	override fun onBindViewHolder(holder: HomeBookItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<HomeBookItem>() {
			override fun areItemsTheSame(oldItem: HomeBookItem, newItem: HomeBookItem) =
				oldItem.bookShelfId == newItem.bookShelfId

			override fun areContentsTheSame(oldItem: HomeBookItem, newItem: HomeBookItem) =
				oldItem == newItem
		}
	}
}