package com.example.bookchat.adapter.agony

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.databinding.ItemAgonyDataBinding
import com.example.bookchat.viewmodel.AgonyViewModel
import com.example.bookchat.viewmodel.AgonyViewModel.PagingViewEvent

class AgonyDataItemAdapter(private val agonyViewModel: AgonyViewModel) :
    PagingDataAdapter<AgonyDataItem, AgonyDataItemAdapter.AgonyDataItemViewHolder>(
        AGONY_ITEM_COMPARATOR
    ) {
    private lateinit var bindingDataItem: ItemAgonyDataBinding
    private lateinit var dataItemClickListener: OnDataItemClickListener

    inner class AgonyDataItemViewHolder(val binding: ItemAgonyDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(agonyDataItem: AgonyDataItem) {
            binding.viewmodel = agonyViewModel
            binding.agonyDataItem = agonyDataItem
            binding.root.setOnClickListener {
                when (agonyDataItem.status) {
                    AgonyDataItemStatus.Default -> {
                        dataItemClickListener.onItemClick(agonyDataItem)
                        return@setOnClickListener
                    }
                    AgonyDataItemStatus.Editing -> {
                        val event = PagingViewEvent.ChangeItemStatusToSelected(agonyDataItem.copy())
                        agonyViewModel.onPagingViewEvent(event)
                    }
                    AgonyDataItemStatus.Selected -> {
                        val event =
                            PagingViewEvent.ChangeItemStatusToSelected(agonyDataItem.copy(status = AgonyDataItemStatus.Editing))
                        agonyViewModel.onPagingViewEvent(event)
                    }
                }

            }

        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_data

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AgonyDataItemViewHolder {
        bindingDataItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_data, parent, false)
        return AgonyDataItemViewHolder(bindingDataItem)
    }

    override fun onBindViewHolder(
        holder: AgonyDataItemViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnDataItemClickListener {
        fun onItemClick(agonyDataItem: AgonyDataItem)
    }

    fun setDataItemClickListener(onDataItemClickListener: OnDataItemClickListener) {
        this.dataItemClickListener = onDataItemClickListener
    }

    companion object {
        val AGONY_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyDataItem>() {
            override fun areItemsTheSame(oldItem: AgonyDataItem, newItem: AgonyDataItem) =
                oldItem.agony.agonyId == newItem.agony.agonyId

            override fun areContentsTheSame(oldItem: AgonyDataItem, newItem: AgonyDataItem) =
                oldItem == newItem
        }
    }
}