package com.example.bookchat.ui.adapter.chatting.chatdrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.ui.adapter.chatting.chatdrawer.ChatRoomDrawerHeaderAdapter.ChatDrawerHeaderItemViewHolder
import com.example.bookchat.databinding.ItemChatDrawerHeaderBinding
import com.example.bookchat.domain.model.Channel

class ChatRoomDrawerHeaderAdapter(var channel: Channel?) :
    RecyclerView.Adapter<ChatDrawerHeaderItemViewHolder>() {

    inner class ChatDrawerHeaderItemViewHolder(val binding: ItemChatDrawerHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            channel?.let { binding.channel = it }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatDrawerHeaderItemViewHolder {
        val binding: ItemChatDrawerHeaderBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_chat_drawer_header,
                parent, false
            )
        return ChatDrawerHeaderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatDrawerHeaderItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1
    override fun getItemViewType(position: Int): Int = R.layout.item_chat_drawer_header
}