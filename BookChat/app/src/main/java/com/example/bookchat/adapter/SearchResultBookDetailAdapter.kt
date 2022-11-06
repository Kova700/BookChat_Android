package com.example.bookchat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ItemBookSearchResultBinding
import com.example.bookchat.utils.Constants.TAG

class SearchResultBookDetailAdapter
    : PagingDataAdapter<Book, SearchResultBookDetailAdapter.BookResultViewHolder>(BOOK_COMPARATOR) {

    private lateinit var binding : ItemBookSearchResultBinding
    private lateinit var itemClickListener: OnItemClickListener

    inner class BookResultViewHolder(val binding: ItemBookSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book : Book){
            Log.d(TAG, "BookResultViewHolder: bind() - ${book.title} 바인드됨")
            binding.book = book
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookResultViewHolder {
        Log.d(TAG, "SearchResultBookAdapter: onCreateViewHolder() - called")
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_book_search_result,
            parent,
            false
        )
        return BookResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookResultViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(book :Book)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        private val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book) =
                oldItem.isbn == newItem.isbn

            override fun areContentsTheSame(oldItem: Book, newItem: Book) =
                oldItem == newItem
        }
    }

}