package com.example.bookchat.ui.adapter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatRoomListDataBinding
import com.example.bookchat.domain.model.Channel

class MainUserChatRoomListAdapter :
    ListAdapter<Channel, MainUserChatRoomListAdapter.ChatRoomItemViewHolder>(
        CHAT_ROOM_LIST_ITEM_COMPARATOR
    ) {

    private lateinit var itemClickListener: OnItemClickListener

    inner class ChatRoomItemViewHolder(val binding: ItemChatRoomListDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: Channel) {
            binding.channel = channel
            binding.root.setOnClickListener {
                itemClickListener.onItemClick(channel)
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
        fun onItemClick(channel: Channel)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        val CHAT_ROOM_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel) =
                oldItem == newItem
        }
    }
}