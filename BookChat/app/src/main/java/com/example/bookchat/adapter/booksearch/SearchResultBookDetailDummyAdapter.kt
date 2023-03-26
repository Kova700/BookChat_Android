package com.example.bookchat.adapter.booksearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemFlexBoxDummyBinding
import com.example.bookchat.utils.BookImgSizeManager

class SearchResultBookDetailDummyAdapter :
    RecyclerView.Adapter<SearchResultBookDetailDummyAdapter.BookResultDummyViewHolder>() {
    private lateinit var binding : ItemFlexBoxDummyBinding
    var dummyItemCount = 0

    inner class BookResultDummyViewHolder(val binding: ItemFlexBoxDummyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookResultDummyViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_flex_box_dummy,parent,false)
        setDummyItemSize()
        return BookResultDummyViewHolder(binding)
    }

    private fun setDummyItemSize(){
        with(binding){
            flexBoxDummyBookLayout.layoutParams.width = BookImgSizeManager.bookImgWidthPx
            flexBoxDummyBookLayout.layoutParams.height = BookImgSizeManager.bookImgHeightPx
        }
    }

    override fun onBindViewHolder(holder: BookResultDummyViewHolder, position: Int) {}
    override fun getItemCount(): Int = dummyItemCount
    override fun getItemViewType(position: Int): Int = R.layout.item_flex_box_dummy
}