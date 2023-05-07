package com.example.bookchat.adapter.chatting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatEntity.ChatType
import com.example.bookchat.databinding.EmptyLayoutBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding

class ChatDataItemAdapter :
    PagingDataAdapter<ChatEntity, RecyclerView.ViewHolder>(CHAT_ITEM_COMPARATOR) {

    inner class MineChatViewHolder(val binding: ItemChattingMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatEntity) {
            binding.chat = chat
        }
    }

    inner class OtherChatViewHolder(val binding: ItemChattingOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatEntity) {
            binding.chat = chat
        }
    }

    inner class NoticeChatViewHolder(val binding: ItemChattingNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatEntity) {
            binding.chat = chat
        }
    }

    inner class EmptyViewHolder(val binding: EmptyLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.item_chatting_mine -> {
                val bindingChatMine: ItemChattingMineBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_mine,
                    parent, false
                )
                return MineChatViewHolder(bindingChatMine)
            }
            R.layout.item_chatting_other -> {
                val bindingChatOther: ItemChattingOtherBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_other,
                    parent, false
                )
                return OtherChatViewHolder(bindingChatOther)
            }
            R.layout.item_chatting_notice -> {
                val bindingChatNotice: ItemChattingNoticeBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_chatting_notice,
                    parent, false
                )
                return NoticeChatViewHolder(bindingChatNotice)
            }
            R.layout.empty_layout -> {
                val bindingEmpty: EmptyLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.empty_layout,
                    parent, false
                )
                return EmptyViewHolder(bindingEmpty)
            }
            else -> throw RuntimeException("Received unknown ViewHolderType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let {
            when (currentItem.chatType) {
                ChatType.Mine -> (holder as MineChatViewHolder).bind(currentItem)
                ChatType.Other -> (holder as OtherChatViewHolder).bind(currentItem)
                ChatType.Notice -> (holder as NoticeChatViewHolder).bind(currentItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == -1 || getItem(position) == null) return R.layout.empty_layout

        return when (getItem(position)?.chatType) {
            ChatType.Mine -> R.layout.item_chatting_mine
            ChatType.Other -> R.layout.item_chatting_other
            ChatType.Notice -> R.layout.item_chatting_notice
            else -> throw RuntimeException("Received unknown ChatType")
        }
    }

    companion object {
        val CHAT_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChatEntity>() {
            override fun areItemsTheSame(oldItem: ChatEntity, newItem: ChatEntity) =
                oldItem.chatId == newItem.chatId

            override fun areContentsTheSame(oldItem: ChatEntity, newItem: ChatEntity) =
                oldItem == newItem
        }
    }
}