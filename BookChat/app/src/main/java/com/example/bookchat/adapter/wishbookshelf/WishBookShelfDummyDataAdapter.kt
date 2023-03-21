package com.example.bookchat.adapter.wishbookshelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R

class WishBookShelfDummyDataAdapter :
    RecyclerView.Adapter<WishBookShelfDummyDataAdapter.WishBookShelfDummyDataViewHolder>() {
    var dummyItemCount = 0

    inner class WishBookShelfDummyDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishBookShelfDummyDataViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_wish_bookshelf_dummy_data, parent, false)
        return WishBookShelfDummyDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishBookShelfDummyDataViewHolder, position: Int) {}
    override fun getItemCount(): Int = dummyItemCount
//    override fun getItemViewType(position: Int): Int = R.layout.item_wish_bookshelf_dummy_data
//    override fun getItemId(position: Int): Long = position.toLong() //일단 보류
}