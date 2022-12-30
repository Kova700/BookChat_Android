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
                firstItemClickListner.onItemClick()
            }
        }
    }

    inner class AgonyDataItemViewHolder(val binding: ItemAgonyDataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(agonyItem : AgonyItem){
            if (agonyItem !is AgonyDataItem) return
            binding.viewmodel = agonyViewModel
            binding.agony = agonyItem.agony
            binding.root.setOnClickListener {
                dataItemClickListener.onItemClick(agonyItem.agony)
            }
        }
    }

    //ViewHolder 타입 구분하고 정의하고,
    //헤더 ViewHolder면 spanSize = 1 로 설정 (책 표지 , 이름 ,작가 정보가 들어가야함)

    //헤더 ViewHolder가 아니면 spanSize = 2로 설정해야함
        //첫번째 아이템은 ViewHolder타입을 일반 데이터 ViewHolder와 구분해서 명시해야함
        //첫번째 아아템은 항상 넣어두고 데이터를 호출하면 일반 데이터 ViewHolder를 추가하는 방식으로 구현

    override fun getItemViewType(position: Int): Int {
        //넘어온 데이터를 열어봤을때 해당 아이템은 해당 ViewHolder와 연결되게
        //내용물을 구분해야함
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
        //viewType타입에 맞게 분기해서 ViewHolder 반환
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

    //AgonyEquatable 인터페이스 정의해서 헤더, firstItem, dataItem 구분가능하게 구현
    companion object {
        val AGONY_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyItem>() {
            override fun areItemsTheSame(oldItem: AgonyItem, newItem: AgonyItem) = when{
                (oldItem is AgonyHeader && newItem is AgonyHeader) -> oldItem.bookShelfItem == newItem.bookShelfItem
                (oldItem is AgonyFirstItem && newItem is AgonyFirstItem) -> oldItem == newItem
                (oldItem is AgonyDataItem && newItem is AgonyDataItem) -> oldItem.agony.agonyId == newItem.agony.agonyId
                else -> false
            }

            override fun areContentsTheSame(oldItem: AgonyItem, newItem: AgonyItem)= when{
                (oldItem is AgonyHeader && newItem is AgonyHeader) -> oldItem == newItem
                (oldItem is AgonyFirstItem && newItem is AgonyFirstItem) -> oldItem == newItem
                (oldItem is AgonyDataItem && newItem is AgonyDataItem) -> oldItem.agony == newItem.agony
                else -> false
            }
        }
    }

}