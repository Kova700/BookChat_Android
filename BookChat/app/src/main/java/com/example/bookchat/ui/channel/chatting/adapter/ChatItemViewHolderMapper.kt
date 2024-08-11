package com.example.bookchat.ui.channel.chatting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding

fun getChatItemViewHolder(
	parent: ViewGroup,
	itemViewType: Int,
	onClickUserProfile: ((Int) -> Unit)? = null,
	onClickFailedChatRetryBtn: ((Int) -> Unit)? = null,
	onClickFailedChatDeleteBtn: ((Int) -> Unit)? = null,
	onSelectCaptureChat: ((Int) -> Unit)? = null,
	onClickMoveToWholeText: ((Int) -> Unit)? = null,
): ChatItemViewHolder {

	return when (itemViewType) {
		R.layout.item_chatting_mine -> {
			val binding = ItemChattingMineBinding.inflate(
				LayoutInflater.from(parent.context),
				parent, false
			)
			MyChatViewHolder(
				binding = binding,
				onClickFailedChatRetryBtn = onClickFailedChatRetryBtn,
				onClickFailedChatDeleteBtn = onClickFailedChatDeleteBtn,
				onSelectCaptureChat = onSelectCaptureChat,
				onClickMoveToWholeText = onClickMoveToWholeText
			)
		}

		R.layout.item_chatting_other -> {
			val binding = ItemChattingOtherBinding.inflate(
				LayoutInflater.from(parent.context),
				parent, false
			)
			AnotherUserChatViewHolder(
				binding = binding,
				onClickUserProfile = onClickUserProfile,
				onSelectCaptureChat = onSelectCaptureChat,
				onClickMoveToWholeText = onClickMoveToWholeText
			)
		}

		R.layout.item_chatting_notice -> {
			val binding = ItemChattingNoticeBinding.inflate(
				LayoutInflater.from(parent.context),
				parent, false
			)
			NoticeChatViewHolder(
				binding = binding,
				onSelectCaptureChat = onSelectCaptureChat,
			)
		}

		R.layout.item_chatting_date -> {
			val binding = ItemChattingDateBinding.inflate(
				LayoutInflater.from(parent.context),
				parent, false
			)
			DateSeparatorViewHolder(
				binding = binding,
				onSelectCaptureChat = onSelectCaptureChat,
			)
		}

		R.layout.item_chatting_last_read_notice -> {
			val binding = ItemChattingLastReadNoticeBinding.inflate(
				LayoutInflater.from(parent.context),
				parent, false
			)
			LastReadNoticeViewHolder(
				binding = binding,
				onSelectCaptureChat = onSelectCaptureChat,
			)
		}

		else -> throw RuntimeException("Received unknown ViewHolderType")
	}
}