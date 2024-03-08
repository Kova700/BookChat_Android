package com.example.bookchat.ui.bookshelf.wish.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemFlexBoxDummyBinding
import javax.inject.Inject

class WishBookShelfDummyDataAdapter @Inject constructor() :
	RecyclerView.Adapter<WishBookShelfDummyDataViewHolder>() {
	var dummyItemCount = 0

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): WishBookShelfDummyDataViewHolder {
		val binding: ItemFlexBoxDummyBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_flex_box_dummy,
			parent, false
		)
		return WishBookShelfDummyDataViewHolder(binding)
	}

	override fun onBindViewHolder(holder: WishBookShelfDummyDataViewHolder, position: Int) {}
	override fun getItemCount(): Int = dummyItemCount
	override fun getItemViewType(position: Int): Int = R.layout.item_flex_box_dummy
}

class WishBookShelfDummyDataViewHolder(val binding: ItemFlexBoxDummyBinding) :
	RecyclerView.ViewHolder(binding.root)