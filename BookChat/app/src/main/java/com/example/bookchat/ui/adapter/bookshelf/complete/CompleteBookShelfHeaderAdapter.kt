package com.example.bookchat.ui.adapter.bookshelf.complete

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookshelfHeaderBinding
import com.example.bookchat.ui.viewmodel.BookShelfViewModel

class CompleteBookShelfHeaderAdapter(private val bookShelfViewModel: BookShelfViewModel) :
    RecyclerView.Adapter<CompleteBookShelfHeaderAdapter.CompleteBookShelfHeaderViewHolder>() {

    private lateinit var bindingHeaderItem: ItemCompleteBookshelfHeaderBinding

    inner class CompleteBookShelfHeaderViewHolder(val binding: ItemCompleteBookshelfHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = bookShelfViewModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CompleteBookShelfHeaderViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_complete_bookshelf_header, parent, false)
        return CompleteBookShelfHeaderViewHolder(bindingHeaderItem)
    }

    override fun onBindViewHolder(holder: CompleteBookShelfHeaderViewHolder, position: Int) {
        holder.bind()
    }
    override fun getItemCount(): Int = 1
    override fun getItemViewType(position: Int): Int = R.layout.item_complete_bookshelf_header
}