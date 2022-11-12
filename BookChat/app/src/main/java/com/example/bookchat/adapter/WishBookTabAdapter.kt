package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.ItemWishBookTabBinding

class WishBookTabAdapter : PagingDataAdapter<BookShelfItem,WishBookTabAdapter.WishBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemWishBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener

    inner class WishBookItemViewHolder(val binding: ItemWishBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(book : BookShelfItem){
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
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(book :BookShelfItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        private val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<BookShelfItem>() {
            override fun areItemsTheSame(oldItem: BookShelfItem, newItem: BookShelfItem) =
                oldItem.isbn == newItem.isbn

            override fun areContentsTheSame(oldItem: BookShelfItem, newItem: BookShelfItem) =
                oldItem == newItem
        }
    }

}