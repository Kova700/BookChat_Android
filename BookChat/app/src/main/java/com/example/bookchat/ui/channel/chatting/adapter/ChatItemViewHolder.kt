package com.example.bookchat.ui.channel.chatting.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChattingDateBinding
import com.example.bookchat.databinding.ItemChattingLastReadNoticeBinding
import com.example.bookchat.databinding.ItemChattingMineBinding
import com.example.bookchat.databinding.ItemChattingNoticeBinding
import com.example.bookchat.databinding.ItemChattingOtherBinding
import com.example.bookchat.domain.model.ChatStatus
import com.example.bookchat.ui.channel.chatting.model.ChatItem
import com.example.bookchat.utils.getFormattedTimeText
import com.example.bookchat.utils.image.loadUserProfile
import com.example.bookchat.utils.isSingleTextOrEmoji

sealed class ChatItemViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(
		chatItem: ChatItem,
		isCaptureMode: Boolean,
	)

	companion object {
		const val MAX_MESSAGE_LENGTH = 500
	}
}

class MyChatViewHolder(
	private val binding: ItemChattingMineBinding,
	private val onClickFailedChatRetryBtn: ((Int) -> Unit)?,
	private val onClickFailedChatDeleteBtn: ((Int) -> Unit)?,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
	private val onClickMoveToWholeText: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {

	init {
		with(binding) {
			failedChatRetryBtn.setOnClickListener {
				onClickFailedChatRetryBtn?.invoke(absoluteAdapterPosition)
			}
			failedChatDeleteBtn.setOnClickListener {
				onClickFailedChatDeleteBtn?.invoke(absoluteAdapterPosition)
			}
			root.setOnClickListener {
				onSelectCaptureChat?.invoke(absoluteAdapterPosition)
			}
			moveToWholeTextBtn.setOnClickListener {
				onClickMoveToWholeText?.invoke(absoluteAdapterPosition)
			}
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
			setMessageTextState(item.message)
			moveToWholeTextBtn.visibility =
				if (item.message.length > MAX_MESSAGE_LENGTH) View.VISIBLE else View.GONE
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
				if (item.dispatchTime.isNotBlank()) getFormattedTimeText(item.dispatchTime)
				else ""
			setCaptureViewState(
				isCaptureMode = isCaptureMode,
				chatItem = item,
				captureLayoutView = chatCaptureLayoutView,
				rootLayout = root
			)
		}
	}

	private fun setMessageTextState(message: String) {
		with(binding) {
			chattingLayout.background =
				if (message.isSingleTextOrEmoji()) null else root.context.getDrawable(R.drawable.chat_bubble_blue)
			chatTv.textSize = if (message.isSingleTextOrEmoji()) 70f else 12f
			chatTv.text =
				if (message.length > MAX_MESSAGE_LENGTH) message.substring(0, MAX_MESSAGE_LENGTH) + "..."
				else message
		}
	}
}

class AnotherUserChatViewHolder(
	private val binding: ItemChattingOtherBinding,
	private val onClickUserProfile: ((Int) -> Unit)?,
	private val onSelectCaptureChat: ((Int) -> Unit)?,
	private val onClickMoveToWholeText: ((Int) -> Unit)?,
) : ChatItemViewHolder(binding) {
	init {
		with(binding) {
			userProfileIv.setOnClickListener {
				onClickUserProfile?.invoke(absoluteAdapterPosition)
			}
			root.setOnClickListener {
				onSelectCaptureChat?.invoke(absoluteAdapterPosition)
			}
			moveToWholeTextBtn.setOnClickListener {
				onClickMoveToWholeText?.invoke(absoluteAdapterPosition)
			}
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
			setAdminItemView(item)
			setCaptureViewState(
				isCaptureMode = isCaptureMode,
				chatItem = item,
				captureLayoutView = binding.chatCaptureLayoutView,
				rootLayout = binding.root
			)
			setMessageTextState(item.message)
			moveToWholeTextBtn.visibility =
				if (item.message.length > MAX_MESSAGE_LENGTH) View.VISIBLE else View.GONE
			uesrNicknameTv.text =
				if (item.sender?.nickname.isNullOrBlank()) root.context.getString(R.string.unknown)
				else item.sender?.nickname
			chatDispatchTimeTv.text =
				if (item.dispatchTime.isNotBlank()) getFormattedTimeText(item.dispatchTime) else ""
			userProfileIv.loadUserProfile(
				imageUrl = item.sender?.profileImageUrl,
				userDefaultProfileType = item.sender?.defaultProfileImageType
			)
			userProfileIv.isClickable = isCaptureMode.not()
		}
	}

	private fun setMessageTextState(message: String) {
		with(binding) {
			chattingLayout.background =
				if (message.isSingleTextOrEmoji()) null else root.context.getDrawable(R.drawable.chat_bubble_gray)
			chatTv.textSize = if (message.isSingleTextOrEmoji()) 70f else 12f
			chatTv.text =
				if (message.length > MAX_MESSAGE_LENGTH) message.substring(0, MAX_MESSAGE_LENGTH) + "..."
				else message
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
		setCaptureViewState(
			isCaptureMode = isCaptureMode,
			chatItem = chatItem,
			captureLayoutView = binding.chatCaptureLayoutView,
			rootLayout = binding.root
		)
		binding.noticeTv.text = item.message
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
		setCaptureViewState(
			isCaptureMode = isCaptureMode,
			chatItem = chatItem,
			captureLayoutView = binding.chatCaptureLayoutView,
			rootLayout = binding.root
		)
		binding.noticeTv.text = item.date
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
	}
}