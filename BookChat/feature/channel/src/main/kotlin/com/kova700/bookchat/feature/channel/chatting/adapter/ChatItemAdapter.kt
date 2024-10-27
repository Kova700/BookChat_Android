package com.kova700.bookchat.feature.channel.chatting.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.core.data.chat.external.model.ChatState
import com.kova700.bookchat.feature.channel.chatting.model.ChatItem
import javax.inject.Inject
import com.kova700.bookchat.feature.channel.R as channelR

class ChatItemAdapter @Inject constructor() :
	ListAdapter<ChatItem, ChatItemViewHolder>(CHAT_ITEM_COMPARATOR) {
	private var isCaptureMode = false

	var onSelectCaptureChat: ((Int) -> Unit)? = null
	var onClickUserProfile: ((Int) -> Unit)? = null
	var onClickFailedChatRetryBtn: ((Int) -> Unit)? = null
	var onClickFailedChatDeleteBtn: ((Int) -> Unit)? = null
	var onClickMoveToWholeText: ((Int) -> Unit)? = null
	var onLongClickChatItem: ((Int) -> Unit)? = null

	val lastReadChatNoticeIndex
		get() = currentList.indexOfFirst { chatItem ->
			(chatItem is ChatItem.LastReadChatNotice)
		}

	val newestChatNotFailedIndex
		get() = currentList.indexOfFirst { chatItem ->
			chatItem is ChatItem.AnotherUser ||
							chatItem is ChatItem.Notification ||
							((chatItem is ChatItem.MyChat) && chatItem.state == ChatState.SUCCESS)
		}

	val newestChatNotMineIndex
		get() = currentList.indexOfFirst { chatItem ->
			(chatItem is ChatItem.AnotherUser) || (chatItem is ChatItem.Notification)
		}

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ChatItem.MyChat -> channelR.layout.item_chatting_mine
			is ChatItem.AnotherUser -> channelR.layout.item_chatting_other
			is ChatItem.DateSeparator -> channelR.layout.item_chatting_date
			is ChatItem.Notification -> channelR.layout.item_chatting_notice
			is ChatItem.LastReadChatNotice -> channelR.layout.item_chatting_last_read_notice
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
			onSelectCaptureChat = onSelectCaptureChat,
			onClickMoveToWholeText = onClickMoveToWholeText,
			onLongClickChatItem = onLongClickChatItem,
		)
	}

	override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
		holder.bind(chatItem = getItem(position), isCaptureMode = isCaptureMode)
	}

	fun onBindViewHolderForCapture(holder: ChatItemViewHolder, position: Int) {
		holder.bind(chatItem = getItem(position), isCaptureMode = false)
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