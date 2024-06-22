package com.example.bookchat.ui.channel.chatting.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.utils.DateManager

sealed class ChatItemViewHolder(
	binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	)
}

class MyChatViewHolder(
	private val binding: ItemChattingMineBinding,
	private val onClickFailedChatRetryBtn: ((Int) -> Unit)?,
	private val onClickFailedChatDeleteBtn: ((Int) -> Unit)?,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {

	init {
		binding.failedChatRetryBtn.setOnClickListener {
			onClickFailedChatRetryBtn?.invoke(absoluteAdapterPosition)
		}
		binding.failedChatDeleteBtn.setOnClickListener {
			onClickFailedChatDeleteBtn?.invoke(absoluteAdapterPosition)
		}
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	) {
		val item = chatItem as ChatItem.MyChat
		setViewState(
			item = item,
			isCaptureMode = isCaptureMode,
		)
	}

	private fun setViewState(
		item: ChatItem.MyChat,
		isCaptureMode: Boolean,
	) {
		with(binding) {
			chat = item
			failedChatRetryBtn.isClickable = isCaptureMode.not()
			failedChatDeleteBtn.isClickable = isCaptureMode.not()
			failedChatBtnLayout.visibility =
				if (item.status == ChatStatus.FAILURE) View.VISIBLE else View.GONE
			chatDispatchTimeTv.visibility =
				if (item.status != ChatStatus.FAILURE) View.VISIBLE else View.INVISIBLE
			chatLoadingIcon.visibility =
				if (item.status == ChatStatus.LOADING || item.status == ChatStatus.RETRY_REQUIRED)
					View.VISIBLE else View.GONE
			chatDispatchTimeTv.text =
				if (item.dispatchTime.isNotBlank()) DateManager.getFormattedTimeText(item.dispatchTime)
				else ""
			setCaptureViewState(
				isCaptureMode = isCaptureMode,
				chatItem = item,
				captureLayoutView = chatCaptureLayoutView,
				rootLayout = root
			)
			executePendingBindings()
		}
	}
}

class AnotherUserChatViewHolder(
	private val binding: ItemChattingOtherBinding,
	private val onClickUserProfile: ((Int) -> Unit)?,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.userProfileIv.setOnClickListener {
			onClickUserProfile?.invoke(absoluteAdapterPosition)
		}
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	) {
		val item = chatItem as ChatItem.AnotherUser
		setViewState(
			item = item,
			isCaptureMode = isCaptureMode,
		)
	}

	private fun setViewState(
		item: ChatItem.AnotherUser,
		isCaptureMode: Boolean,
	) {
		with(binding) {
			chat = item
			setAdminItemView(item)
			setCaptureViewState(
				isCaptureMode = isCaptureMode,
				chatItem = item,
				captureLayoutView = binding.chatCaptureLayoutView,
				rootLayout = binding.root
			)
			chatDispatchTimeTv.text =
				if (item.dispatchTime.isNotBlank()) DateManager.getFormattedTimeText(item.dispatchTime)
				else ""
			userProfileIv.isClickable = isCaptureMode.not()
			executePendingBindings()
		}
	}

	private fun setAdminItemView(item: ChatItem.AnotherUser) {
		with(binding) {
			hostCrown.visibility = if (item.isTargetUserHost) View.VISIBLE else View.GONE
			subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
		}
	}
}

class NoticeChatViewHolder(
	private val binding: ItemChattingNoticeBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	) {
		val item = chatItem as ChatItem.Notification
		binding.chat = item
		setCaptureViewState(
			isCaptureMode = isCaptureMode,
			chatItem = chatItem,
			captureLayoutView = binding.chatCaptureLayoutView,
			rootLayout = binding.root
		)
		binding.executePendingBindings()
	}
}

class DateSeparatorViewHolder(
	private val binding: ItemChattingDateBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	) {
		val item = chatItem as ChatItem.DateSeparator
		binding.dateString = item.date
		setCaptureViewState(
			isCaptureMode = isCaptureMode,
			chatItem = chatItem,
			captureLayoutView = binding.chatCaptureLayoutView,
			rootLayout = binding.root
		)
		binding.executePendingBindings()
	}
}

class LastReadNoticeViewHolder(
	private val binding: ItemChattingLastReadNoticeBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	) {
		setCaptureViewState(
			isCaptureMode = isCaptureMode,
			chatItem = chatItem,
			captureLayoutView = binding.chatCaptureLayoutView,
			rootLayout = binding.root
		)
		binding.executePendingBindings()
	}
}