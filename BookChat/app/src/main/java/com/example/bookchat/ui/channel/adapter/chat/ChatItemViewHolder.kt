package com.example.bookchat.ui.channel.adapter.chat

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.ui.channel.model.chat.ChatItem

sealed class ChatItemViewHolder(
	binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(chatItem: ChatItem)
}

class MyChatViewHolder(
	private val binding: ItemChattingMineBinding,
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.MyChat
		binding.chat = item
	}
}

class AnotherUserChatViewHolder(
	private val binding: ItemChattingOtherBinding,
	private val onClickUserProfile: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.userProfileCv.setOnClickListener {
			onClickUserProfile?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.AnotherUser
		binding.chat = item
		setAdminItemView(item)
	}

	private fun setAdminItemView(item: ChatItem.AnotherUser) {
		binding.hostCrown.visibility = if (item.isTargetUserHost) View.VISIBLE else View.GONE
		binding.subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
	}
}

class NoticeChatViewHolder(
	private val binding: ItemChattingNoticeBinding,
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.Notification
		binding.chat = item
	}
}

class DateSeparatorViewHolder(
	private val binding: ItemChattingDateBinding,
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.DateSeparator
		binding.dateString = item.date
	}
}

class LastReadNoticeViewHolder(
	private val binding: ItemChattingLastReadNoticeBinding,
) : ChatItemViewHolder(binding) {
	override fun bind(chatItem: ChatItem) {}
}