package com.example.bookchat.ui.bookshelf.reading.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookshelfHeaderBinding
import javax.inject.Inject

class ReadingBookShelfHeaderAdapter @Inject constructor() :
	RecyclerView.Adapter<ReadingBookShelfHeaderViewHolder>() {
	var totalItemCount: Int = 0

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ReadingBookShelfHeaderViewHolder {
		val binding: ItemReadingBookshelfHeaderBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_reading_bookshelf_header,
			parent, false
		)
		return ReadingBookShelfHeaderViewHolder(binding, totalItemCount)
	}

	override fun onBindViewHolder(holder: ReadingBookShelfHeaderViewHolder, position: Int) {
		holder.bind()
	}

	override fun getItemCount(): Int = 1
	override fun getItemViewType(position: Int): Int = R.layout.item_reading_bookshelf_header
}

class ReadingBookShelfHeaderViewHolder(
	val binding: ItemReadingBookshelfHeaderBinding,
	val totalItemCount: Int
) : RecyclerView.ViewHolder(binding.root) {
	fun bind() {
		binding.totalItemCount = totalItemCount
	}
}