package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentReadingBookTabBinding
import com.example.bookchat.databinding.ItemReadingBookTabBinding

class ReadingBookTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private lateinit var binding : ItemReadingBookTabBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_reading_book_tab,parent,false)
        return ReadingBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ReadingBookItemViewHolder){
        }
    }

    override fun getItemCount(): Int {
        return 5
    }

    inner class ReadingBookItemViewHolder(val binding: ItemReadingBookTabBinding)
        : RecyclerView.ViewHolder(binding.root){

    }

}