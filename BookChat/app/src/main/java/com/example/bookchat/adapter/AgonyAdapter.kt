package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.databinding.ItemAgonyDataBinding
import com.example.bookchat.databinding.ItemAgonyFirstBinding
import com.example.bookchat.databinding.ItemAgonyHeaderBinding
import com.example.bookchat.viewmodel.AgonyViewModel
import com.example.bookchat.viewmodel.AgonyViewModel.PagingViewEvent

class AgonyAdapter(private val agonyViewModel : AgonyViewModel)
    : PagingDataAdapter<AgonyItem, RecyclerView.ViewHolder>(AGONY_ITEM_COMPARATOR){
    private lateinit var bindingDataItem : ItemAgonyDataBinding
    private lateinit var bindingFirstItem : ItemAgonyFirstBinding
    private lateinit var bindingHeaderItem : ItemAgonyHeaderBinding

    private lateinit var dataItemClickListener : OnDataItemClickListener
    private lateinit var firstItemClickListner : OnFirstItemClickListener

    inner class AgonyHeaderItemViewHolder(val binding: ItemAgonyHeaderBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(){
            binding.viewmodel = agonyViewModel
        }
    }

    inner class AgonyFirstItemViewHolder(val binding: ItemAgonyFirstBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(){
            binding.root.setOnClickListener{
                if(agonyViewModel.activityStateFlow.value != AgonyViewModel.AgonyActivityState.Default) return@setOnClickListener
                firstItemClickListner.onItemClick()
            }
        }
    }

    inner class AgonyDataItemViewHolder(val binding: ItemAgonyDataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(agonyItem : AgonyItem){
            if (agonyItem !is AgonyDataItem) return
            binding.viewmodel = agonyViewModel
            binding.agonyDataItem = agonyItem
            binding.root.setOnClickListener {
                when(agonyItem.status){
                    AgonyDataItemStatus.Default -> {
                        dataItemClickListener.onItemClick(agonyItem.agony)
                        return@setOnClickListener
                    }
                    AgonyDataItemStatus.Editing -> {
                        val event = PagingViewEvent.ChangeItemStatusToSelected(agonyItem.copy())
                        agonyViewModel.onPagingViewEvent(event)
                    }
                    AgonyDataItemStatus.Selected -> {
                        val event = PagingViewEvent.ChangeItemStatusToSelected(agonyItem.copy(status = AgonyDataItemStatus.Editing))
                        agonyViewModel.onPagingViewEvent(event)
                    }
                }

            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is AgonyHeader -> R.layout.item_agony_header
            is AgonyFirstItem -> R.layout.item_agony_first
            is AgonyDataItem -> R.layout.item_agony_data
            else -> throw Exception("Unknown ViewType")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when(viewType){
            R.layout.item_agony_header -> {
                bindingHeaderItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_header,parent,false)
                return AgonyHeaderItemViewHolder(bindingHeaderItem)
            }
            R.layout.item_agony_first -> {
                bindingFirstItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_first,parent,false)
                return AgonyFirstItemViewHolder(bindingFirstItem)
            }
            R.layout.item_agony_data -> {
                bindingDataItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_data,parent,false)
                return AgonyDataItemViewHolder(bindingDataItem)
            }
            else -> throw Exception("Unknown ViewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        when(currentItem) {
            is AgonyHeader -> (holder as AgonyHeaderItemViewHolder).bind()
            is AgonyFirstItem -> (holder as AgonyFirstItemViewHolder).bind()
            is AgonyDataItem -> (holder as AgonyDataItemViewHolder).bind(currentItem)
        }
    }

    interface OnDataItemClickListener {
        fun onItemClick(agony : Agony)
    }

    interface OnFirstItemClickListener {
        fun onItemClick()
    }

    fun setDataItemClickListener(onDataItemClickListener: OnDataItemClickListener) {
        this.dataItemClickListener = onDataItemClickListener
    }

    fun setFirstItemClickListner(onFirstItemClickListener: OnFirstItemClickListener){
        this.firstItemClickListner = onFirstItemClickListener
    }

    companion object {
        val AGONY_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyItem>() {
            override fun areItemsTheSame(oldItem: AgonyItem, newItem: AgonyItem) = when{
                (oldItem is AgonyHeader && newItem is AgonyHeader) -> oldItem.bookShelfItem == newItem.bookShelfItem
                (oldItem is AgonyFirstItem && newItem is AgonyFirstItem) -> oldItem == newItem
                (oldItem is AgonyDataItem && newItem is AgonyDataItem) -> oldItem.agony == newItem.agony
                else -> false
            }

            override fun areContentsTheSame(oldItem: AgonyItem, newItem: AgonyItem) = when{
                (oldItem is AgonyHeader && newItem is AgonyHeader) -> oldItem == newItem
                (oldItem is AgonyFirstItem && newItem is AgonyFirstItem) -> oldItem == newItem
                (oldItem is AgonyDataItem && newItem is AgonyDataItem) -> oldItem == newItem
                else -> false
            }
        }
    }

}