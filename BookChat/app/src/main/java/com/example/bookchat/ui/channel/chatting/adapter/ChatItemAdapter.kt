package com.example.bookchat.ui.channel.chatting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import javax.inject.Inject

class ChatItemAdapter @Inject constructor() :
	ListAdapter<ChatItem, ChatItemViewHolder>(CHAT_ITEM_COMPARATOR) {
	var onClickUserProfile: ((Int) -> Unit)? = null

	val lastReadChatNoticeIndex
		get() = currentList.indexOfFirst { chatItem ->
			(chatItem is ChatItem.LastReadChatNotice)
		}

	val newestChatNotFailedIndex
		get() = currentList.indexOfFirst { chatItem ->
			chatItem is ChatItem.AnotherUser ||
							chatItem is ChatItem.Notification ||
							((chatItem is ChatItem.MyChat) && chatItem.status == ChatStatus.SUCCESS)
		}

	val newestChatNotMineIndex
		get() = currentList.indexOfFirst { chatItem ->
			(chatItem is ChatItem.AnotherUser) || (chatItem is ChatItem.Notification)
		}

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ChatItem.MyChat -> R.layout.item_chatting_mine
			is ChatItem.AnotherUser -> R.layout.item_chatting_other
			is ChatItem.DateSeparator -> R.layout.item_chatting_date
			is ChatItem.Notification -> R.layout.item_chatting_notice
			is ChatItem.LastReadChatNotice -> R.layout.item_chatting_last_read_notice
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ChatItemViewHolder {
		when (viewType) {
			R.layout.item_chatting_mine -> {
				val binding: ItemChattingMineBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_chatting_mine,
					parent, false
				)
				return MyChatViewHolder(binding)
			}

			R.layout.item_chatting_other -> {
				val binding: ItemChattingOtherBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_chatting_other,
					parent, false
				)
				return AnotherUserChatViewHolder(binding, onClickUserProfile)
			}

			R.layout.item_chatting_notice -> {
				val binding: ItemChattingNoticeBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_chatting_notice,
					parent, false
				)
				return NoticeChatViewHolder(binding)
			}

			R.layout.item_chatting_date -> {
				val binding: ItemChattingDateBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_chatting_date,
					parent, false
				)
				return DateSeparatorViewHolder(binding)
			}

			R.layout.item_chatting_last_read_notice -> {
				val binding: ItemChattingLastReadNoticeBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_chatting_last_read_notice,
					parent, false
				)
				return LastReadNoticeViewHolder(binding)
			}

			else -> throw RuntimeException("Received unknown ViewHolderType")
		}
	}

	override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val CHAT_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChatItem>() {
			override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem) =
				oldItem == newItem
		}
	}
}