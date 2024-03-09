package com.example.bookchat.ui.search.adapter.booksearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemBookSearchResultBinding
import com.example.bookchat.domain.model.Book
import com.example.bookchat.utils.BookImgSizeManager

class SearchResultBookSimpleDataAdapter :
	RecyclerView.Adapter<SearchResultBookSimpleDataAdapter.BookResultViewHolder>() {

	private lateinit var binding: ItemBookSearchResultBinding
	private lateinit var itemClickListener: OnItemClickListener
	var books: List<Book> = listOf()

	inner class BookResultViewHolder(val binding: ItemBookSearchResultBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(book: Book) {
			binding.book = book
			binding.root.setOnClickListener {
				itemClickListener.onItemClick(book)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookResultViewHolder {
		binding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context),
			R.layout.item_book_search_result,
			parent,
			false
		)
		return BookResultViewHolder(binding)
	}

	override fun onBindViewHolder(holder: BookResultViewHolder, position: Int) {
		if (books.isNotEmpty()) holder.bind(books[position])
	}

	override fun getItemCount(): Int {
		return minOf(books.size, BookImgSizeManager.flexBoxBookSpanSize * 2)
	}

	override fun getItemViewType(position: Int): Int = R.layout.item_book_search_result

	interface OnItemClickListener {
		fun onItemClick(book: Book)
	}

	fun setItemClickListener(onItemClickListener: OnItemClickListener) {
		this.itemClickListener = onItemClickListener
	}
}