package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemReadingBookTabBinding
import com.example.bookchat.databinding.ItemWishBookTabBinding
import com.example.bookchat.databinding.ItemWishBookTabBindingImpl

class WishBookTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private lateinit var binding : ItemWishBookTabBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_wish_book_tab,parent,false)
        return WishBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WishBookItemViewHolder){

        }
    }

    override fun getItemCount(): Int {
        return 5
    }

    inner class WishBookItemViewHolder(val binding: ItemWishBookTabBinding)
        : RecyclerView.ViewHolder(binding.root){

    }
}