package com.example.bookchat.ui.adapter.search.booksearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemFlexBoxDummyBinding

class SearchResultBookDummyAdapter :
    RecyclerView.Adapter<SearchResultBookDummyAdapter.BookResultDummyViewHolder>() {
    private lateinit var binding : ItemFlexBoxDummyBinding
    var dummyItemCount = 0

    inner class BookResultDummyViewHolder(val binding: ItemFlexBoxDummyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookResultDummyViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_flex_box_dummy,parent,false)
        return BookResultDummyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookResultDummyViewHolder, position: Int) {}
    override fun getItemCount(): Int = dummyItemCount
    override fun getItemViewType(position: Int): Int = R.layout.item_flex_box_dummy
}