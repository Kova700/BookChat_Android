package com.example.bookchat.adapter.chatting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.Chat
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding

class ChatDataItemAdapter :
    ListAdapter<Chat, RecyclerView.ViewHolder>(CHAT_ITEM_COMPARATOR) {

    private lateinit var bindingChatMine: ItemChattingMineBinding
    private lateinit var bindingChatOther: ItemChattingOtherBinding
    private lateinit var bindingChatNotice: ItemChattingNoticeBinding

    inner class MineChatViewHolder(val binding: ItemChattingMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
        }
    }

    inner class OtherChatViewHolder(val binding: ItemChattingOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
        }
    }

    inner class NoticeChatViewHolder(val binding: ItemChattingNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.chat = chat
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_chatting_mine -> {
                bindingChatMine = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_mine,
                    parent, false
                )
                return MineChatViewHolder(bindingChatMine)
            }
            R.layout.item_chatting_other -> {
                bindingChatOther = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_other,
                    parent, false
                )
                return OtherChatViewHolder(bindingChatOther)
            }
            R.layout.item_chatting_notice -> {
                bindingChatNotice = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_notice,
                    parent, false
                )
                return NoticeChatViewHolder(bindingChatNotice)
            }
            else -> throw RuntimeException("Received unknown ViewHolderType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        when (currentItem.getChatType()) {
            R.layout.item_chatting_mine -> (holder as MineChatViewHolder).bind(currentItem)
            R.layout.item_chatting_other -> (holder as OtherChatViewHolder).bind(currentItem)
            R.layout.item_chatting_notice -> (holder as NoticeChatViewHolder).bind(currentItem)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).getChatType()

    companion object {
        val CHAT_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(oldItem: Chat, newItem: Chat) =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(oldItem: Chat, newItem: Chat) =
                oldItem == newItem
        }
    }
}