package com.example.bookchat.ui.adapter.chatting.chatdrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.local.entity.UserEntity
import com.example.bookchat.databinding.ItemChatDrawerDataBinding

class ChatRoomDrawerDataAdapter :
    ListAdapter<UserEntity, ChatRoomDrawerDataAdapter.ChatDrawerDataItemViewHolder>(
        USER_ENTITY_COMPARATOR
    ) {

    inner class ChatDrawerDataItemViewHolder(val binding: ItemChatDrawerDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserEntity) {
            binding.user = user
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatDrawerDataItemViewHolder {
        val binding: ItemChatDrawerDataBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_chat_drawer_data,
                parent, false
            )
        return ChatDrawerDataItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatDrawerDataItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_drawer_data

    companion object {
        val USER_ENTITY_COMPARATOR = object : DiffUtil.ItemCallback<UserEntity>() {
            override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity) =
                oldItem == newItem
        }
    }
}