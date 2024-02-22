package com.example.bookchat.ui.adapter.userchatroomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.database.model.ChatRoomEntity
import com.example.bookchat.databinding.ItemChatRoomListDataBinding

class UserChatRoomListDataAdapter :
    PagingDataAdapter<ChatRoomEntity, UserChatRoomListDataAdapter.ChatRoomItemViewHolder>(
        CHAT_ROOM_LIST_ITEM_COMPARATOR
    ) {

    private lateinit var itemClickListener: OnItemClickListener

    inner class ChatRoomItemViewHolder(val binding: ItemChatRoomListDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoomEntity: ChatRoomEntity) {
            binding.chatRoom = chatRoomEntity
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(chatRoomEntity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomItemViewHolder {
        val bindingDataItem: ItemChatRoomListDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_chat_room_list_data,
            parent, false
        )
        return ChatRoomItemViewHolder(bindingDataItem)
    }

    override fun onBindViewHolder(holder: ChatRoomItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(chatRoomEntity: ChatRoomEntity)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_list_data

    companion object {
        val CHAT_ROOM_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChatRoomEntity>() {
            override fun areItemsTheSame(oldItem: ChatRoomEntity, newItem: ChatRoomEntity) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: ChatRoomEntity, newItem: ChatRoomEntity) =
                oldItem == newItem
        }
    }
}