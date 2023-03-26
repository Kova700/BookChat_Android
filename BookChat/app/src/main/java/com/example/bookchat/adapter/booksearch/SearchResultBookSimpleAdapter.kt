package com.example.bookchat.adapter.booksearch

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ItemBookSearchResultBinding
import com.example.bookchat.utils.Constants

class SearchResultBookSimpleAdapter : RecyclerView.Adapter<SearchResultBookSimpleAdapter.BookResultViewHolder>(){

    private lateinit var binding :ItemBookSearchResultBinding
    private lateinit var itemClickListener : OnItemClickListener
    var books :List<Book> = listOf()

    inner class BookResultViewHolder(val binding: ItemBookSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book : Book){
            Log.d(Constants.TAG, "BookResultViewHolder: bind() - ${book.title} 바인드됨")
            binding.book = book
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookResultViewHolder {
        Log.d(Constants.TAG, "SearchResultBookAdapter: onCreateViewHolder() - called")
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_book_search_result,
            parent,
            false
        )
        return BookResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookResultViewHolder, position: Int) {
        if(books.isNotEmpty()) holder.bind(books[position])
    }

    override fun getItemCount(): Int {
        return books.size
    }

    interface OnItemClickListener {
        fun onItemClick(book :Book)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }


}