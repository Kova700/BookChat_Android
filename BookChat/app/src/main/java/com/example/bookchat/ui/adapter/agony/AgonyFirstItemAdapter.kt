package com.example.bookchat.ui.adapter.agony

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyFirstBinding
import com.example.bookchat.ui.viewmodel.AgonyViewModel

class AgonyFirstItemAdapter(private val agonyViewModel: AgonyViewModel) :
    RecyclerView.Adapter<AgonyFirstItemAdapter.AgonyFirstItemViewHolder>() {

    private lateinit var bindingFirstItem: ItemAgonyFirstBinding
    private lateinit var firstItemClickListner: OnFirstItemClickListener

    inner class AgonyFirstItemViewHolder(val binding: ItemAgonyFirstBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.root.setOnClickListener {
                if (agonyViewModel.activityStateFlow.value != AgonyViewModel.AgonyActivityState.Default) return@setOnClickListener
                firstItemClickListner.onItemClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgonyFirstItemViewHolder {
        bindingFirstItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_agony_first, parent, false)
        return AgonyFirstItemViewHolder(bindingFirstItem)
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_agony_first

    override fun onBindViewHolder(holder: AgonyFirstItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    fun setFirstItemClickListner(onFirstItemClickListener: OnFirstItemClickListener) {
        this.firstItemClickListner = onFirstItemClickListener
    }

    interface OnFirstItemClickListener {
        fun onItemClick()
    }
}