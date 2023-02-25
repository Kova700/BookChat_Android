package com.example.bookchat.adapter.agonyrecord

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.AgonyRecordFirstItemStatus
import com.example.bookchat.databinding.ItemAgonyRecordFirstBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiEvent
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiState

class AgonyRecordFirstItemAdapter(private val agonyRecordViewModel: AgonyRecordViewModel) :
    RecyclerView.Adapter<AgonyRecordFirstItemAdapter.AgonyRecordFirstItemViewHolder>(){

    private lateinit var bindingFirstItem: ItemAgonyRecordFirstBinding

    inner class AgonyRecordFirstItemViewHolder(val binding: ItemAgonyRecordFirstBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            with(binding){
                viewmodel = agonyRecordViewModel
                root.setOnClickListener {
                    when(agonyRecordViewModel.agonyRecordUiState.value){
                        AgonyRecordUiState.Default -> {
                            agonyRecordViewModel.firstItemState.value = AgonyRecordFirstItemStatus.Editing
                            agonyRecordViewModel.setUiState(AgonyRecordUiState.Editing)
                            notifyItemChanged(0)
                        }
                        AgonyRecordUiState.Editing -> {
                            if (agonyRecordViewModel.firstItemState.value != AgonyRecordFirstItemStatus.Editing){
                                //경고 다이얼로그 띄우기
                                agonyRecordViewModel.startEvent(AgonyRecordUiEvent.ShowWarningDialog)
                            }
                        }
                    }
                }
            }
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
    override fun getItemCount(): Int = 1
    override fun onBindViewHolder(holder: AgonyRecordFirstItemViewHolder, position: Int) {
        holder.bind()
    }
}