package com.example.bookchat.adapter.AgonyRecord

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

            //연필누르면 고민 제목 수정되게 구현(API호출 해야함)
            //setOnClickListener
            //현재 Item의 상태에 따라 분기
            //Editing -> Default 상태로 변경
            //Default -> Editing 상태로 변경
            //눌리면 EditText나와야함  + (고민명 수정 API 호출)

            //버튼을 누르면 상태를 바꾸는 Event를 넣어야 UI가 바뀐다.
            //Item의 Status만 바꾸면 UI가 갱신이 안된다.
            // -> notiftItemChanged 호출해서 해당 아이템에 변화가 일어났다고 알려야한다.
            // -> Pagging3라이브러리로 인해 해당 UI가 ViewModel에 캐싱되어 있음으로 (x)
            // -> 데이터가 변했다고 인지하지 못해서 UI를 갱신하지 않는거다.
            // -> 그럼 만다꼬 DiffUtil을 쓰나 (그럼 이거 왜씀?)
            // -> 데이터를 다시 가져오거나 dataSet이 변경되었을때 데이터들을 구분하기 위해서..?
            // ->
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