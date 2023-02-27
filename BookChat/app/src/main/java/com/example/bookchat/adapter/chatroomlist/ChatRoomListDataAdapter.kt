package com.example.bookchat.adapter.chatroomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.ChatRoomListItem
import com.example.bookchat.databinding.ItemChatRoomListDataBinding

class ChatRoomListDataAdapter :
    PagingDataAdapter<ChatRoomListItem, ChatRoomListDataAdapter.ChatDataViewHolder>(CHAT_ROOM_LIST_ITEM_COMPARATOR) {

    private lateinit var bindingDataItem: ItemChatRoomListDataBinding

    inner class ChatDataViewHolder(val binding: ItemChatRoomListDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoomListItem: ChatRoomListItem) {
            binding.chatRoomListItem = chatRoomListItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatDataViewHolder {
        bindingDataItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_chat_room_list_data,
                parent, false)
        return ChatDataViewHolder(bindingDataItem)
    }

    override fun onBindViewHolder(holder: ChatDataViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_list_data

    companion object {
        val CHAT_ROOM_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChatRoomListItem>() {
            override fun areItemsTheSame(oldItem: ChatRoomListItem, newItem: ChatRoomListItem) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: ChatRoomListItem, newItem: ChatRoomListItem) =
                oldItem == newItem
        }
    }
}