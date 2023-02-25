package com.example.bookchat.adapter.readingbookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookshelfHeaderBinding
import com.example.bookchat.viewmodel.BookShelfViewModel

class ReadingBookShelfHeaderAdapter(private val bookShelfViewModel: BookShelfViewModel) :
    RecyclerView.Adapter<ReadingBookShelfHeaderAdapter.ReadingBookShelfHeaderViewHolder>()
{
    private lateinit var bindingHeaderItem: ItemReadingBookshelfHeaderBinding

    inner class ReadingBookShelfHeaderViewHolder(val binding: ItemReadingBookshelfHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = bookShelfViewModel

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReadingBookShelfHeaderViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context),
                R.layout.item_reading_bookshelf_header, parent,false)
        return ReadingBookShelfHeaderViewHolder(bindingHeaderItem)
    }

    override fun onBindViewHolder(holder: ReadingBookShelfHeaderViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
    override fun getItemViewType(position: Int): Int = R.layout.item_reading_bookshelf_header
}