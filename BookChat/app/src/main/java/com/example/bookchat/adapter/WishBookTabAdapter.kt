package com.example.bookchat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ItemWishBookTabBinding
import com.example.bookchat.utils.Constants

class WishBookTabAdapter : RecyclerView.Adapter<WishBookTabAdapter.WishBookItemViewHolder>(){
    private lateinit var binding : ItemWishBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener
    var books :List<BookShelfItem> = listOf()

    inner class WishBookItemViewHolder(val binding: ItemWishBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book : BookShelfItem){
            Log.d(Constants.TAG, "BookResultViewHolder: bind() - ${book.title} 바인드됨")
            binding.bookShelfItem = book
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_wish_book_tab,parent,false)
        return WishBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishBookItemViewHolder, position: Int) {
        if(books.isNotEmpty()) holder.bind(books[position])
    }

    override fun getItemCount(): Int {
        return books.size
    }

    interface OnItemClickListener {
        fun onItemClick(book :BookShelfItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

}