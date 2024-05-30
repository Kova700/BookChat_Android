package com.example.bookchat.ui.channel.adapter.chat

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.ui.channel.model.chat.ChatItem

sealed class ChatItemViewHolder(
	binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(chatItem: ChatItem)
}

class MyChatViewHolder(
	val binding: ItemChattingMineBinding
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.MyChat
		binding.chat = item
	}
}

class AnotherUserChatViewHolder(
	val binding: ItemChattingOtherBinding
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.AnotherUser
		binding.chat = item
	}
}

class NoticeChatViewHolder(
	val binding: ItemChattingNoticeBinding
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.Notification
		binding.chat = item
	}
}

class DateSeparatorViewHolder(
	val binding: ItemChattingDateBinding
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.DateSeparator
		binding.dateString = item.date
	}
}

class LastReadNoticeViewHolder(
	val binding: ItemChattingLastReadNoticeBinding
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {}
}