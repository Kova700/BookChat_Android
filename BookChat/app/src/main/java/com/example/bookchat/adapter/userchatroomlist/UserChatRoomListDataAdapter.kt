package com.example.bookchat.adapter.userchatroomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.UserChatRoomListItem
import com.example.bookchat.databinding.ItemChatRoomListDataBinding

class UserChatRoomListDataAdapter :
    PagingDataAdapter<UserChatRoomListItem, UserChatRoomListDataAdapter.ChatRoomItemViewHolder>(CHAT_ROOM_LIST_ITEM_COMPARATOR) {

    private lateinit var bindingDataItem: ItemChatRoomListDataBinding

    inner class ChatRoomItemViewHolder(val binding: ItemChatRoomListDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userChatRoomListItem: UserChatRoomListItem) {
            binding.userChatRoomListItem = userChatRoomListItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomItemViewHolder {
        bindingDataItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_chat_room_list_data,
                parent, false)
        return ChatRoomItemViewHolder(bindingDataItem)
    }

    override fun onBindViewHolder(holder: ChatRoomItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_list_data

    companion object {
        val CHAT_ROOM_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<UserChatRoomListItem>() {
            override fun areItemsTheSame(oldItem: UserChatRoomListItem, newItem: UserChatRoomListItem) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: UserChatRoomListItem, newItem: UserChatRoomListItem) =
                oldItem == newItem
        }
    }
}