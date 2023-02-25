package com.example.bookchat.adapter.wish_bookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.ItemWishBookshelfDataBinding

class WishBookShelfDataAdapter : PagingDataAdapter<BookShelfDataItem, WishBookShelfDataAdapter.WishBookItemViewHolder>(
    BOOK_SHELF_ITEM_COMPARATOR
){
    private lateinit var binding : ItemWishBookshelfDataBinding
    private lateinit var itemClickListener : OnItemClickListener

    inner class WishBookItemViewHolder(val binding: ItemWishBookshelfDataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(bookShelfDataItem : BookShelfDataItem){
            binding.bookShelfItem = bookShelfDataItem.bookShelfItem
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(bookShelfDataItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_data,parent,false)
        return WishBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishBookItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(bookShelfDataItem :BookShelfDataItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_data

    companion object {
        val BOOK_SHELF_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<BookShelfDataItem>() {
            override fun areItemsTheSame(oldItem: BookShelfDataItem, newItem: BookShelfDataItem) =
                oldItem.bookShelfItem.bookShelfId == newItem.bookShelfItem.bookShelfId

            override fun areContentsTheSame(oldItem: BookShelfDataItem, newItem: BookShelfDataItem) =
                oldItem.bookShelfItem == newItem.bookShelfItem
        }
    }

}