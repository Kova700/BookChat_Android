package com.example.bookchat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.databinding.ItemAgonyRecordDataBinding
import com.example.bookchat.databinding.ItemAgonyRecordFirstBinding
import com.example.bookchat.databinding.ItemAgonyRecordHeaderBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel

class AgonyRecordAdapter(private val agonyRecordViewModel: AgonyRecordViewModel) :
    PagingDataAdapter<AgonyRecordItem, RecyclerView.ViewHolder>(AGONY_RECORD_ITEM_COMPARATOR) {
    private lateinit var bindingHeaderItem: ItemAgonyRecordHeaderBinding
    private lateinit var bindingFirstItem: ItemAgonyRecordFirstBinding
    private lateinit var bindingDataItem: ItemAgonyRecordDataBinding

    inner class AgonyRecordHeaderItemViewHolder(val binding: ItemAgonyRecordHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = agonyRecordViewModel
            //연필누르면 고민 제목 수정되게 구현(API호출 해야함)
        }
    }

    inner class AgonyRecordFirstItemViewHolder(val binding: ItemAgonyRecordFirstBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = agonyRecordViewModel
            //setOnClickListener

            //연필누르면 고민 제목 수정되게 구현(API호출 해야함)
            //눌리면 고민기록 입력창 UI 나와야함 + (등록 API 호출)
        }
    }

    inner class AgonyRecordDataItemViewHolder(val binding: ItemAgonyRecordDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(agonyRecordItem :AgonyRecordItem) {
            if(agonyRecordItem !is AgonyRecordDataItem) return
            binding.viewmodel = agonyRecordViewModel
            binding.agonyRecordDataItem = agonyRecordItem
            //setOnClickListener

            //연필누르면 고민 제목 수정되게 구현(API호출 해야함)
            //눌리면 수정 UI 나와야함 + (수정 API 호출)
            //꾹 눌리면 삭제 UI 나와야함
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is AgonyRecordHeader -> R.layout.item_agony_record_header
            is AgonyRecordFirstItem -> R.layout.item_agony_record_first
            is AgonyRecordDataItem -> R.layout.item_agony_record_data
            else -> throw Exception("Unknown ViewType")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when(viewType){
            R.layout.item_agony_record_header -> {
                bindingHeaderItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_header,parent,false)
                return AgonyRecordHeaderItemViewHolder(bindingHeaderItem)
            }
            R.layout.item_agony_record_first -> {
                bindingFirstItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_first,parent,false)
                return AgonyRecordFirstItemViewHolder(bindingFirstItem)
            }
            R.layout.item_agony_record_data -> {
                bindingDataItem = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_data,parent,false)
                return AgonyRecordDataItemViewHolder(bindingDataItem)
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
            is AgonyRecordHeader -> (holder as AgonyRecordHeaderItemViewHolder).bind()
            is AgonyRecordFirstItem -> (holder as AgonyRecordFirstItemViewHolder).bind()
            is AgonyRecordDataItem -> (holder as AgonyRecordDataItemViewHolder).bind(currentItem)
        }
    }

    companion object {
        val AGONY_RECORD_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyRecordItem>() {
            override fun areItemsTheSame(oldItem: AgonyRecordItem, newItem: AgonyRecordItem) = when{
                (oldItem is AgonyRecordHeader && newItem is AgonyRecordHeader) -> oldItem.agony == newItem.agony
                (oldItem is AgonyRecordFirstItem && newItem is AgonyRecordFirstItem) -> oldItem == newItem
                (oldItem is AgonyRecordDataItem && newItem is AgonyRecordDataItem) ->
                    oldItem.agonyRecord.agonyRecordId == newItem.agonyRecord.agonyRecordId
                else -> false
            }

            override fun areContentsTheSame(oldItem: AgonyRecordItem, newItem: AgonyRecordItem) = when{
                (oldItem is AgonyRecordHeader && newItem is AgonyRecordHeader) -> oldItem == newItem
                (oldItem is AgonyRecordFirstItem && newItem is AgonyRecordFirstItem) -> oldItem == newItem
                (oldItem is AgonyRecordDataItem && newItem is AgonyRecordDataItem) -> oldItem == newItem
                else -> false
            }
        }
    }
}