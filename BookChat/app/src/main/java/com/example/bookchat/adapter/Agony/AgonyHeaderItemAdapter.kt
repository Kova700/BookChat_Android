package com.example.bookchat.adapter.Agony

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyHeaderBinding
import com.example.bookchat.viewmodel.AgonyViewModel

class AgonyHeaderItemAdapter(private val agonyViewModel: AgonyViewModel) :
    RecyclerView.Adapter<AgonyHeaderItemAdapter.AgonyHeaderItemViewHolder>() {

    private lateinit var bindingHeaderItem: ItemAgonyHeaderBinding

    inner class AgonyHeaderItemViewHolder(val binding: ItemAgonyHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = agonyViewModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgonyHeaderItemViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_header, parent, false)
        return AgonyHeaderItemViewHolder(bindingHeaderItem)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_header

    override fun onBindViewHolder(holder: AgonyHeaderItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
}