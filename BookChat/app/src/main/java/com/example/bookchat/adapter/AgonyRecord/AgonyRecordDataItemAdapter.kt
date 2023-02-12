package com.example.bookchat.adapter.AgonyRecord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.SwipeHelperCallback.Companion.SWIPE_VIEW_PERCENT
import com.example.bookchat.data.AgonyRecordDataItem
import com.example.bookchat.data.AgonyRecordDataItemStatus
import com.example.bookchat.data.AgonyRecordFirstItemStatus
import com.example.bookchat.databinding.ItemAgonyRecordDataBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiEvent
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiState
import com.example.bookchat.viewmodel.AgonyRecordViewModel.PagingViewEvent

class AgonyRecordDataItemAdapter(private val agonyRecordViewModel: AgonyRecordViewModel) :
    PagingDataAdapter<AgonyRecordDataItem, AgonyRecordDataItemAdapter.AgonyRecordDataItemViewHolder>(AGONY_RECORD_ITEM_COMPARATOR) {
    private lateinit var bindingDataItem: ItemAgonyRecordDataBinding

    inner class AgonyRecordDataItemViewHolder(val binding: ItemAgonyRecordDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(agonyRecordItem :AgonyRecordDataItem) {
            with(binding){
                agonyRecordDataItem = agonyRecordItem
                setViewHolderState(swipeView, agonyRecordItem)

                swipeView.setOnClickListener {
                    when(agonyRecordViewModel.agonyRecordUiState.value){
                        AgonyRecordUiState.Default -> {
                            if(!agonyRecordItem.isSwiped){
                                val itemStatusChangeEvent = PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Editing))
                                agonyRecordViewModel.addPagingViewEvent(itemStatusChangeEvent)
                                agonyRecordViewModel.setUiState(AgonyRecordUiState.Editing)
                            }
                        }
                        AgonyRecordUiState.Editing -> {
                            if (agonyRecordViewModel.isFirstItemStatusEditing()){
                                if (agonyRecordViewModel.doesFirstItemHaveData()){
                                    agonyRecordViewModel.startEvent(AgonyRecordUiEvent.ShowWarningDialog)
                                    return@setOnClickListener
                                }
                                agonyRecordViewModel.renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
                                agonyRecordViewModel.clearFirstItemData()
                                val itemStatusChangeEvent = PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Editing))
                                agonyRecordViewModel.addPagingViewEvent(itemStatusChangeEvent)
                                agonyRecordViewModel.setUiState(AgonyRecordUiState.Editing)
                                return@setOnClickListener
                            }
                            agonyRecordViewModel.startEvent(AgonyRecordUiEvent.ShowWarningDialog)
                        }
                    }
                }

                with(swipeBackground){
                    background.setOnClickListener {
                        if (!agonyRecordItem.isSwiped) return@setOnClickListener
                        agonyRecordViewModel.deleteAgonyRecord(agonyRecordItem)
                    }
                }

                with(editLayout){
                    editCancelBtn.setOnClickListener{
                        agonyRecordViewModel.resetAllEditingItemToDefault()
                        agonyRecordViewModel.setUiState(AgonyRecordUiState.Default)
                    }

                    submitBtn.setOnClickListener{
                        if ((newTitle?.isBlank() == true) || (newContent?.isBlank() == true)){
                            agonyRecordViewModel.makeToast("제목, 내용을 입력해주세요.")
                            return@setOnClickListener
                        }

                        val loadingEvent = PagingViewEvent.ItemStatusChange(agonyRecordItem.copy(status = AgonyRecordDataItemStatus.Loading))
                        agonyRecordViewModel.addPagingViewEvent(loadingEvent)

                        if((newTitle!!.trim() == agonyRecordItem.agonyRecord.agonyRecordTitle) &&
                                newContent!!.trim() == agonyRecordItem.agonyRecord.agonyRecordContent){
                            agonyRecordViewModel.removePagingViewEvent(loadingEvent)
                            agonyRecordViewModel.resetAllEditingItemToDefault()
                            agonyRecordViewModel.setUiState(AgonyRecordUiState.Default)
                            return@setOnClickListener
                        }
                        agonyRecordViewModel.reviseAgonyRecord(agonyRecordItem, newTitle!!, newContent!!)
                    }
                }
            }
        }

        fun setSwiped(flag :Boolean){
            val currentItem = getItem(bindingAdapterPosition)
            currentItem?.let { currentItem.isSwiped = flag }
        }

        fun getSwiped(): Boolean{
            val currentItem = getItem(bindingAdapterPosition)
            return currentItem?.isSwiped ?: false
        }
    }

    private fun setViewHolderState(view: View, agonyRecordItem :AgonyRecordDataItem){
        if(!agonyRecordItem.isSwiped) { view.translationX = 0f; return }
        view.translationX = view.width.toFloat() * SWIPE_VIEW_PERCENT
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