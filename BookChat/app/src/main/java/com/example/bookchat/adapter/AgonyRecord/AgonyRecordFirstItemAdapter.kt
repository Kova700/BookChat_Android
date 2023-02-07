package com.example.bookchat.adapter.AgonyRecord

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyRecordFirstBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel

class AgonyRecordFirstItemAdapter(private val agonyRecordViewModel: AgonyRecordViewModel) :
    RecyclerView.Adapter<AgonyRecordFirstItemAdapter.AgonyRecordFirstItemViewHolder>(){

    private lateinit var bindingFirstItem: ItemAgonyRecordFirstBinding

    inner class AgonyRecordFirstItemViewHolder(val binding: ItemAgonyRecordFirstBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.viewmodel = agonyRecordViewModel
            //setOnClickListener
            //현재 Item의 상태에 따라 분기
            //Editing -> Default 상태로 변경
            //Default -> Editing 상태로 변경
            //눌리면 고민기록 입력창 UI 나와야함 + (등록 API 호출)
            //수정 완료 버튼에 연결된 함수에 파라미터 넣어서 수정 or 등록 분기 처리
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AgonyRecordFirstItemViewHolder {
        bindingFirstItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_record_first,parent,false)
        return AgonyRecordFirstItemViewHolder(bindingFirstItem)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_record_first

    override fun onBindViewHolder(holder: AgonyRecordFirstItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
}