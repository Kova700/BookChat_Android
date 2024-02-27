package com.example.bookchat.ui.adapter.chatting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.EmptyLayoutBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.domain.model.ChatType
import com.example.bookchat.utils.DateManager

class ChatDataItemAdapter :
	ListAdapter<Chat, RecyclerView.ViewHolder>(CHAT_ITEM_COMPARATOR) {

	inner class MineChatViewHolder(val binding: ItemChattingMineBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(chat: Chat, isSameDate: Boolean) {
			binding.chat = chat
			if (chat.status == ChatStatus.SUCCESS) binding.isSameDate = isSameDate
		}
	}

	inner class OtherChatViewHolder(val binding: ItemChattingOtherBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(chat: Chat, isSameDate: Boolean) {
			binding.chat = chat
			if (chat.status == ChatStatus.SUCCESS) binding.isSameDate = isSameDate
		}
	}

	inner class NoticeChatViewHolder(val binding: ItemChattingNoticeBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(chat: Chat, isSameDate: Boolean) {
			binding.chat = chat
			if (chat.status == ChatStatus.SUCCESS) binding.isSameDate = isSameDate
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

	//TODO : isSameDate이면 날짜를 Visibility 설정하지 말고, ViewLayout을 Add하는 느낌으로 수정
	// (불가능하면 그냥 두고 가능하면 수정하도록 노력해보자.)
	// 그렇게 안하면 위에 OnClickListener설정할때, DataLayout, MessageLayout 리스너 따로따로 지정해줘야함
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val currentItem = getItem(position)
		val isSameDate = if (position != itemCount - 1) {
			val previousItem = getItem(position + 1)
			DateManager.isSameDate(
				currentItem?.dispatchTime,
				previousItem?.dispatchTime
			)
		} else false

		currentItem?.let {
			when (currentItem.chatType) {
				ChatType.Mine -> (holder as MineChatViewHolder).bind(currentItem, isSameDate)
				ChatType.Other -> (holder as OtherChatViewHolder).bind(currentItem, isSameDate)
				ChatType.Notice -> (holder as NoticeChatViewHolder).bind(currentItem, isSameDate)
				ChatType.UNKNOWN -> Unit
			}
		}
	}

	override fun getItemViewType(position: Int): Int {
		if (position == -1 || getItem(position) == null) return R.layout.empty_layout

		return when (getItem(position)?.chatType) {
			ChatType.Mine -> R.layout.item_chatting_mine
			ChatType.Other -> R.layout.item_chatting_other
			ChatType.Notice -> R.layout.item_chatting_notice
			ChatType.UNKNOWN -> R.layout.empty_layout
			else -> throw RuntimeException("Received unknown ChatType")
		}
	}

	companion object {
		val CHAT_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Chat>() {
			override fun areItemsTheSame(oldItem: Chat, newItem: Chat) =
				oldItem.chatId == newItem.chatId

			override fun areContentsTheSame(oldItem: Chat, newItem: Chat) =
				oldItem == newItem
		}
	}
}