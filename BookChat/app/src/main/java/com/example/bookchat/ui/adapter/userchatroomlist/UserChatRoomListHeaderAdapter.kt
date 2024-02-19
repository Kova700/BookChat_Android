package com.example.bookchat.ui.adapter.userchatroomlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatRoomListHeaderBinding

class UserChatRoomListHeaderAdapter : RecyclerView.Adapter<UserChatRoomListHeaderAdapter.ChatHeaderViewHolder>() {

    private lateinit var bindingHeaderItem: ItemChatRoomListHeaderBinding

    inner class ChatHeaderViewHolder(val binding: ItemChatRoomListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHeaderViewHolder {
        bindingHeaderItem = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context),
                R.layout.item_chat_room_list_header, parent, false)
        return ChatHeaderViewHolder(bindingHeaderItem)
    }

    override fun onBindViewHolder(holder: ChatHeaderViewHolder, position: Int) {}
    override fun getItemCount(): Int = 1
    override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_list_header
}