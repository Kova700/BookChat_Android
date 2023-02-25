package com.example.bookchat.adapter.wishbookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemWishBookshelfHeaderBinding
import com.example.bookchat.viewmodel.BookShelfViewModel

class WishBookShelfHeaderAdapter(private val bookShelfViewModel: BookShelfViewModel) :
    RecyclerView.Adapter<WishBookShelfHeaderAdapter.WishBookShelfHeaderViewHolder>()
{
    private lateinit var bindingHeaderItem: ItemWishBookshelfHeaderBinding

    inner class WishBookShelfHeaderViewHolder(val binding: ItemWishBookshelfHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = bookShelfViewModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishBookShelfHeaderViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_wish_bookshelf_header, parent,false)
        return WishBookShelfHeaderViewHolder(bindingHeaderItem)
    }

    override fun onBindViewHolder(holder: WishBookShelfHeaderViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
    override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_header
}