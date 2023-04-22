package com.example.bookchat.adapter.chatting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.App
import com.example.bookchat.R
import com.example.bookchat.data.Chat
import com.example.bookchat.databinding.ItemChattingMineBinding

class ChatDataItemAdapter :
    ListAdapter<Chat, ChatDataItemAdapter.ChatViewHolder>(CHAT_ITEM_COMPARATOR) {
    val mynickName = App.instance.getCachedUser().userNickname

    //일단 내 채팅만 보이게라도 구현해보자
    private lateinit var binding: ItemChattingMineBinding

    inner class ChatViewHolder(val binding: ItemChattingMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        binding = DataBindingUtil
            .inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chatting_mine,
                parent,
                false
            )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    override fun getItemViewType(position: Int): Int =
        R.layout.item_chatting_mine

    companion object {
        val CHAT_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat) =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat) =
                oldItem == newItem
        }
    }
}