package com.example.bookchat.adapter.AgonyRecord

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.databinding.ItemAgonyRecordDataBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel

class AgonyRecordDataItemAdapter(private val agonyRecordViewModel: AgonyRecordViewModel) :
    PagingDataAdapter<AgonyRecordDataItem, AgonyRecordDataItemAdapter.AgonyRecordDataItemViewHolder>(AGONY_RECORD_ITEM_COMPARATOR) {
    private lateinit var bindingDataItem: ItemAgonyRecordDataBinding

    inner class AgonyRecordDataItemViewHolder(val binding: ItemAgonyRecordDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(agonyRecordItem :AgonyRecordDataItem) {
            binding.viewmodel = agonyRecordViewModel
            binding.agonyRecordDataItem = agonyRecordItem
            //setOnClickListener
                //현재 Item의 상태에 따라 분기
                //Editing -> Default 상태로 변경
                //Default -> Editing 상태로 변경
            //눌리면 고민기록 수정 UI 나와야함(기존 데이터 가지고) + (수정 API 호출)
            //수정 완료 버튼에 연결된 함수에 파라미터 넣어서 수정 or 등록 분기 처리

            //밀어서 삭제 구현해야함
        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_record_data

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AgonyRecordDataItemViewHolder {
        bindingDataItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_data,parent,false)
        return AgonyRecordDataItemViewHolder(bindingDataItem)
    }

    override fun onBindViewHolder(
        holder: AgonyRecordDataItemViewHolder,
        position: Int
    ) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    companion object {
        val AGONY_RECORD_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyRecordDataItem>() {
            override fun areItemsTheSame(oldItem: AgonyRecordDataItem, newItem: AgonyRecordDataItem) =
                oldItem.agonyRecord.agonyRecordId == newItem.agonyRecord.agonyRecordId

            override fun areContentsTheSame(oldItem: AgonyRecordDataItem, newItem: AgonyRecordDataItem) =
                oldItem == newItem
        }
    }
}