package com.example.bookchat.ui.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import javax.inject.Inject

class WishBookShelfHeaderAdapter @Inject constructor() :
	RecyclerView.Adapter<WishBookShelfHeaderViewHolder>() {
	var totalItemCount: Int = 0

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): WishBookShelfHeaderViewHolder {
		val binding: ItemWishBookshelfHeaderBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_header,
			parent, false
		)
		return WishBookShelfHeaderViewHolder(binding, totalItemCount)
	}

	override fun onBindViewHolder(holder: WishBookShelfHeaderViewHolder, position: Int) {
		holder.bind()
	}

	override fun getItemCount(): Int = 1
	override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_header
}

class WishBookShelfHeaderViewHolder(
	val binding: ItemWishBookshelfHeaderBinding,
	val totalItemCount: Int
) : RecyclerView.ViewHolder(binding.root) {
	fun bind() {
		binding.totalItemCount = totalItemCount
	}
}