package com.example.bookchat.ui.agonyrecode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.Agony
import com.example.bookchat.databinding.ItemAgonyRecordHeaderBinding

class AgonyRecordHeaderItemAdapter(var agony : Agony) :
    RecyclerView.Adapter<AgonyRecordHeaderItemAdapter.AgonyRecordHeaderItemViewHolder>(){

    private lateinit var bindingHeaderItem: ItemAgonyRecordHeaderBinding
    private lateinit var headerItemClickListener: OnHeaderItemClickListener

    inner class AgonyRecordHeaderItemViewHolder(val binding: ItemAgonyRecordHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.agony = this@AgonyRecordHeaderItemAdapter.agony
            binding.agonyTitleEidtBtn.setOnClickListener {
                headerItemClickListener.onItemClick()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AgonyRecordHeaderItemViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_header,parent,false)
        return AgonyRecordHeaderItemViewHolder(bindingHeaderItem)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_record_header

    override fun onBindViewHolder(holder: AgonyRecordHeaderItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    fun interface OnHeaderItemClickListener {
        fun onItemClick()
    }

    fun setHeaderItemClickListener(onHeaderItemClickListener: OnHeaderItemClickListener) {
        this.headerItemClickListener = onHeaderItemClickListener
    }
}