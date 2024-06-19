package com.example.bookchat.ui.channel.chatting.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingDateCaptureBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeCaptureBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingMineCaptureBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingNoticeCaptureBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.databinding.ItemChattingOtherCaptureBinding
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.utils.DateManager

//TODO :
// 문제점 2 : 선택되지 않은 애들 중 chat_default_layout로 설정되는 애들이 있음
// 문제점 3 : 클릭된 탑 바텀 사이 미들도 배경도 chat_capture_only_one_layout로 설정됨
// 문제점 4 : 3개 남은 상황에서 가운데 누르면 변경이 안됨
// 문제점 6 : 선택된 애들 개수 한정이 필요함 (20개 정도?)
// 문제점 7 : 날짜에는 선택 배경조차 안생김
// 문제점 8 : 아래 채팅 입력창도 그림자가 생겨야함

sealed class ChatItemViewHolder(
	binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(chatItem: ChatItem)
}

class MyChatViewHolder(
	private val binding: ItemChattingMineBinding,
	private val onClickFailedChatRetryBtn: ((Int) -> Unit)?,
	private val onClickFailedChatDeleteBtn: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {

	init {
		binding.failedChatRetryBtn.setOnClickListener {
			onClickFailedChatRetryBtn?.invoke(absoluteAdapterPosition)
		}
		binding.failedChatDeleteBtn.setOnClickListener {
			onClickFailedChatDeleteBtn?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.MyChat
		binding.chat = item
		setViewState(item)
	}

	private fun setViewState(item: ChatItem.MyChat) {
		binding.failedChatBtnLayout.visibility =
			if (item.status == ChatStatus.FAILURE) View.VISIBLE else View.GONE
		binding.chatDispatchTimeTv.visibility =
			if (item.status != ChatStatus.FAILURE) View.VISIBLE else View.INVISIBLE
		binding.chatLoadingIcon.visibility =
			if (item.status == ChatStatus.LOADING || item.status == ChatStatus.RETRY_REQUIRED)
				View.VISIBLE else View.GONE
		binding.chatDispatchTimeTv.text =
			if (item.dispatchTime.isNotBlank()) DateManager.getFormattedTimeText(item.dispatchTime)
			else ""
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
		binding.chatDispatchTimeTv.text =
			if (item.dispatchTime.isNotBlank()) DateManager.getFormattedTimeText(item.dispatchTime)
			else ""
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

class MyChatCaptureViewHolder(
	private val binding: ItemChattingMineCaptureBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {

	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.MyChat
		binding.chat = item
		binding.executePendingBindings()
		setViewState(item)
		setCaptureViewState(chatItem)
	}

	private fun setCaptureViewState(chatItem: ChatItem) {
		when {
			chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
			}

			chatItem.isCaptureHeader -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_header_layout)
			}

			chatItem.isCaptureMiddle -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_middle_layout)
			}

			chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
			}

			else -> {
				binding.chatCaptureLayoutView.visibility = View.VISIBLE
				binding.root.setBackgroundResource(R.drawable.chat_default_layout)
			}
		}
	}

	private fun setViewState(item: ChatItem.MyChat) {
		with(binding.itemChattingMineLayout) {
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
		}
	}
}

class AnotherUserChatCaptureViewHolder(
	private val binding: ItemChattingOtherCaptureBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.AnotherUser
		binding.chat = item
		binding.executePendingBindings()
		setAdminItemView(item)
		setCaptureViewState(chatItem)
	}

	private fun setCaptureViewState(chatItem: ChatItem) {
		when {
			chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
			}

			chatItem.isCaptureHeader -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_header_layout)
			}

			chatItem.isCaptureMiddle -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_middle_layout)
			}

			chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
			}

			else -> {
				binding.chatCaptureLayoutView.visibility = View.VISIBLE
				binding.root.setBackgroundResource(R.drawable.chat_default_layout)
			}
		}
	}

	private fun setAdminItemView(item: ChatItem.AnotherUser) {
		with(binding.itemChattingOtherLayout) {
			hostCrown.visibility = if (item.isTargetUserHost) View.VISIBLE else View.GONE
			subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
			chatDispatchTimeTv.text =
				if (item.dispatchTime.isNotBlank()) DateManager.getFormattedTimeText(item.dispatchTime)
				else ""
		}
	}
}

class NoticeChatCaptureViewHolder(
	private val binding: ItemChattingNoticeCaptureBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.Notification
		binding.chat = item
		binding.executePendingBindings()
		setCaptureViewState(chatItem)
	}

	private fun setCaptureViewState(chatItem: ChatItem) {
		when {
			chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
			}

			chatItem.isCaptureHeader -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_header_layout)
			}

			chatItem.isCaptureMiddle -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_middle_layout)
			}

			chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
			}

			else -> {
				binding.chatCaptureLayoutView.visibility = View.VISIBLE
				binding.root.setBackgroundResource(R.drawable.chat_default_layout)
			}
		}
	}
}

class DateSeparatorCaptureViewHolder(
	private val binding: ItemChattingDateCaptureBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		val item = chatItem as ChatItem.DateSeparator
		binding.dateString = item.date
		binding.executePendingBindings()
		setCaptureViewState(chatItem)
	}

	private fun setCaptureViewState(chatItem: ChatItem) {
		when {
			chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
			}

			chatItem.isCaptureHeader -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_header_layout)
			}

			chatItem.isCaptureMiddle -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_middle_layout)
			}

			chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
			}

			else -> {
				binding.chatCaptureLayoutView.visibility = View.VISIBLE
				binding.root.setBackgroundResource(R.drawable.chat_default_layout)
			}
		}
	}
}

class LastReadNoticeCaptureViewHolder(
	private val binding: ItemChattingLastReadNoticeCaptureBinding,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onSelectCaptureChat?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(chatItem: ChatItem) {
		setCaptureViewState(chatItem)
	}

	private fun setCaptureViewState(chatItem: ChatItem) {
		when {
			chatItem.isCaptureHeader && chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_only_one_layout)
			}

			chatItem.isCaptureHeader -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_header_layout)
			}

			chatItem.isCaptureMiddle -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_middle_layout)
			}

			chatItem.isCaptureBottom -> {
				binding.chatCaptureLayoutView.visibility = View.GONE
				binding.root.setBackgroundResource(R.drawable.chat_capture_bottom_layout)
			}

			else -> {
				binding.chatCaptureLayoutView.visibility = View.VISIBLE
				binding.root.setBackgroundResource(R.drawable.chat_default_layout)
			}
		}
	}
}