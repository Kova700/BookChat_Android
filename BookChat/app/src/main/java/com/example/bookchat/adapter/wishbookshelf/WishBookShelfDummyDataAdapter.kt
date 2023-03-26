package com.example.bookchat.adapter.wishbookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemWishBookshelfDummyDataBinding
import com.example.bookchat.utils.BookImgSizeManager

class WishBookShelfDummyDataAdapter :
    RecyclerView.Adapter<WishBookShelfDummyDataAdapter.WishBookShelfDummyDataViewHolder>() {
    private lateinit var binding :ItemWishBookshelfDummyDataBinding
    var dummyItemCount = 0

    inner class WishBookShelfDummyDataViewHolder(val binding: ItemWishBookshelfDummyDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishBookShelfDummyDataViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_wish_bookshelf_dummy_data,parent,false)
        setDummyItemSize()
        return WishBookShelfDummyDataViewHolder(binding)
    }

    private fun setDummyItemSize(){
        with(binding){
            flexBoxDummyBookLayout.layoutParams.width = BookImgSizeManager.bookImgWidthPx
            flexBoxDummyBookLayout.layoutParams.height = BookImgSizeManager.bookImgHeightPx
        }
    }

    override fun onBindViewHolder(holder: WishBookShelfDummyDataViewHolder, position: Int) {}
    override fun getItemCount(): Int = dummyItemCount
    override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_dummy_data
}