package com.example.bookchat.ui.adapter.bookshelf.wish

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemFlexBoxDummyBinding

class WishBookShelfDummyDataAdapter :
    RecyclerView.Adapter<WishBookShelfDummyDataAdapter.WishBookShelfDummyDataViewHolder>() {
    private lateinit var binding :ItemFlexBoxDummyBinding
    var dummyItemCount = 0

    inner class WishBookShelfDummyDataViewHolder(val binding: ItemFlexBoxDummyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishBookShelfDummyDataViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_flex_box_dummy,parent,false)
        return WishBookShelfDummyDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishBookShelfDummyDataViewHolder, position: Int) {}
    override fun getItemCount(): Int = dummyItemCount
    override fun getItemViewType(position: Int): Int = R.layout.item_flex_box_dummy
}