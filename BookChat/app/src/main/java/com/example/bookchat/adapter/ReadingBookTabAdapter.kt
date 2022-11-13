package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.adapter.WishBookTabAdapter.Companion.BOOK_SHELF_ITEM_COMPARATOR
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ItemReadingBookTabBinding

class ReadingBookTabAdapter : PagingDataAdapter<BookShelfItem, ReadingBookTabAdapter.ReadingBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemReadingBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener
    private lateinit var pageBtnClickListener :OnItemClickListener

    inner class ReadingBookItemViewHolder(val binding: ItemReadingBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book : BookShelfItem){
            binding.bookShelfItem = book
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(book)
            }
            binding.pageBtn.setOnClickListener{
                pageBtnClickListener.onItemClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingBookTabAdapter.ReadingBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_reading_book_tab,parent,false)
        return ReadingBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReadingBookTabAdapter.ReadingBookItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(book :BookShelfItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun setpageBtnClickListener(onItemClickListener: OnItemClickListener){
        this.pageBtnClickListener = onItemClickListener
    }



}