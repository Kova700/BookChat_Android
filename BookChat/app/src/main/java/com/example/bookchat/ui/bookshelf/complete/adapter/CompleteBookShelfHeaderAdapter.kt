package com.example.bookchat.ui.bookshelf.complete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookshelfHeaderBinding
import javax.inject.Inject

class CompleteBookShelfHeaderAdapter @Inject constructor() :
	RecyclerView.Adapter<CompleteBookShelfHeaderViewHolder>() {

	var totalItemCount: Int = 0

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): CompleteBookShelfHeaderViewHolder {
		val binding: ItemCompleteBookshelfHeaderBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_complete_bookshelf_header,
			parent, false
		)
		return CompleteBookShelfHeaderViewHolder(binding, totalItemCount)
	}

	override fun onBindViewHolder(holder: CompleteBookShelfHeaderViewHolder, position: Int) {
		holder.bind()
	}

	override fun getItemCount(): Int = 1
	override fun getItemViewType(position: Int): Int = R.layout.item_complete_bookshelf_header
}

class CompleteBookShelfHeaderViewHolder(
	val binding: ItemCompleteBookshelfHeaderBinding,
	val totalItemCount: Int
) : RecyclerView.ViewHolder(binding.root) {
	fun bind() {
		binding.totalItemCount = totalItemCount
	}
}