package com.example.bookchat.ui.channel.chatting.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import javax.inject.Inject

class ChatItemAdapter @Inject constructor() :
	ListAdapter<ChatItem, ChatItemViewHolder>(CHAT_ITEM_COMPARATOR) {
	private var isCaptureMode = false

	var onSelectCaptureChat: ((Int) -> Unit)? = null
	var onClickUserProfile: ((Int) -> Unit)? = null
	var onClickFailedChatRetryBtn: ((Int) -> Unit)? = null
	var onClickFailedChatDeleteBtn: ((Int) -> Unit)? = null

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
		if (isCaptureMode) {
			return when (getItem(position)) {
				is ChatItem.MyChat -> R.layout.item_chatting_mine_capture
				is ChatItem.AnotherUser -> R.layout.item_chatting_other_capture
				is ChatItem.DateSeparator -> R.layout.item_chatting_date_capture
				is ChatItem.Notification -> R.layout.item_chatting_notice_capture
				is ChatItem.LastReadChatNotice -> R.layout.item_chatting_last_read_notice_capture
			}
		}

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
		viewType: Int,
	): ChatItemViewHolder {
		return getChatItemViewHolder(
			parent = parent,
			itemViewType = viewType,
			onClickUserProfile = onClickUserProfile,
			onClickFailedChatRetryBtn = onClickFailedChatRetryBtn,
			onClickFailedChatDeleteBtn = onClickFailedChatDeleteBtn,
			onSelectCaptureChat = onSelectCaptureChat
		)
	}

	override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	fun changeToDefaultMode() {
		if (isCaptureMode.not()) return
		isCaptureMode = false
		notifyItemRangeChanged(0, itemCount)
	}

	fun changeToCaptureMode() {
		if (isCaptureMode) return
		isCaptureMode = true
		notifyItemRangeChanged(0, itemCount)
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