package com.example.bookchat.ui.home.book.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemHomeBookBinding
import com.example.bookchat.ui.home.book.model.HomeBookItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl

class HomeBookItemViewHolder(
	private val binding: ItemHomeBookBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	fun bind(homeBookItem: HomeBookItem) {
		binding.bookImg.loadUrl(homeBookItem.book.bookCoverImageUrl)
	}

}