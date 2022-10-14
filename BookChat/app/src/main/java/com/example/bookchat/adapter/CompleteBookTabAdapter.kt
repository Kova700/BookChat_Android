package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemCompleteBookTabBinding

class CompleteBookTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private lateinit var binding : ItemCompleteBookTabBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_complete_book_tab,parent,false)
        return CompleteBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is CompleteBookItemViewHolder){
        }
    }

    override fun getItemCount(): Int {
        return 5
    }

    inner class CompleteBookItemViewHolder(val binding: ItemCompleteBookTabBinding)
        : RecyclerView.ViewHolder(binding.root){

    }
}