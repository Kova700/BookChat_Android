package com.example.bookchat.ui.home.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemHomeBookBinding
import com.example.bookchat.databinding.ItemHomeBookDummyBinding
import com.example.bookchat.databinding.ItemHomeBookEmptyBinding
import com.example.bookchat.databinding.ItemHomeBookHeaderBinding
import com.example.bookchat.databinding.ItemHomeBookRetryBinding
import com.example.bookchat.databinding.ItemHomeChannelBinding
import com.example.bookchat.databinding.ItemHomeChannelEmptyBinding
import com.example.bookchat.databinding.ItemHomeChannelHeaderBinding
import com.example.bookchat.databinding.ItemHomeChannelRetryBinding
import com.example.bookchat.databinding.ItemHomeHeaderBinding
import com.example.bookchat.databinding.LayoutHomeBookShimmerBinding
import com.example.bookchat.databinding.LayoutHomeChannelShimmerBinding
import com.example.bookchat.ui.home.model.HomeItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.getFormattedDetailDateTimeText
import com.example.bookchat.utils.image.loadChannelProfile
import com.example.bookchat.utils.image.loadUrl

sealed class HomeItemViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(homeItem: HomeItem)
}

class HomeHeaderViewHolder(
	private val binding: ItemHomeHeaderBinding,
) : HomeItemViewHolder(binding) {
	override fun bind(homeItem: HomeItem) {
		val item = homeItem as HomeItem.Header
		with(binding) {
			nicknameTv.text =
				root.context.getString(R.string.user_nickname, item.clientNickname)
		}
	}
}

class HomeBookHeaderViewHolder(
	private val binding: ItemHomeBookHeaderBinding,
) : HomeItemViewHolder(binding) {
	override fun bind(homeItem: HomeItem) {}
}

class HomeBookViewHolder(
	private val binding: ItemHomeBookBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onClickBookItem: ((Int) -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onClickBookItem?.invoke(absoluteAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(homeItem: HomeItem) {
		val item = homeItem as HomeItem.BookItem
		binding.bookImg.loadUrl(item.book.bookCoverImageUrl)
	}
}

class HomeBookEmptyViewHolder(
	private val binding: ItemHomeBookEmptyBinding,
	private val onClickBookAddBtn: (() -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.bookAddBtn.setOnClickListener {
			onClickBookAddBtn?.invoke()
		}
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeBookRetryViewHolder(
	private val binding: ItemHomeBookRetryBinding,
	private val onClickRetryBookLoadBtn: (() -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.retryBtn.setOnClickListener {
			onClickRetryBookLoadBtn?.invoke()
		}
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeBookDummyViewHolder(
	private val binding: ItemHomeBookDummyBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : HomeItemViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.root)
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeBookShimmerViewHolder(
	private val binding: LayoutHomeBookShimmerBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : HomeItemViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.shimmerBook1.bookImg)
		bookImgSizeManager.setBookImgSize(binding.shimmerBook2.bookImg)
		bookImgSizeManager.setBookImgSize(binding.shimmerBook3.bookImg)
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeChannelHeaderViewHolder(
	private val binding: ItemHomeChannelHeaderBinding,
) : HomeItemViewHolder(binding) {
	override fun bind(homeItem: HomeItem) {}
}

class HomeChannelViewHolder(
	private val binding: ItemHomeChannelBinding,
	private val onClickChannelItem: ((Int) -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onClickChannelItem?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(homeItem: HomeItem) {
		val channel = (homeItem as HomeItem.ChannelItem)
		with(binding) {
			dispatchTimeTv.text =
				channel.lastChat?.dispatchTime?.let { getFormattedDetailDateTimeText(it) }
			uncheckedChatCountTv.text = if (channel.isExistNewChat) "New+" else ""
			muteChannelIcon.visibility =
				if ((channel.notificationFlag.not()) && channel.isAvailableChannel) View.VISIBLE else View.GONE
			topPinChannelIcon.visibility =
				if ((channel.isTopPined) && channel.isAvailableChannel) View.VISIBLE else View.GONE
			unavailableChannelStateGroup.visibility =
				if (channel.isAvailableChannel.not()) View.VISIBLE else View.GONE
			channelImageIv.loadChannelProfile(
				imageUrl = channel.roomImageUri,
				channelDefaultImageType = channel.defaultRoomImageType
			)
			channelTitleTv.text = channel.roomName
			lastChatContentTv.text = channel.lastChat?.message
			channelMemberCountTv.text =
				root.context.getString(R.string.channel_member_count, channel.roomMemberCount)
		}
	}
}

class HomeChannelEmptyViewHolder(
	private val binding: ItemHomeChannelEmptyBinding,
	private val onClickChannelAddBtn: (() -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.channelAddBtn.setOnClickListener {
			onClickChannelAddBtn?.invoke()
		}
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeChannelRetryViewHolder(
	private val binding: ItemHomeChannelRetryBinding,
	private val onClickRetryChannelLoadBtn: (() -> Unit)?,
) : HomeItemViewHolder(binding) {
	init {
		binding.retryBtn.setOnClickListener {
			onClickRetryChannelLoadBtn?.invoke()
		}
	}

	override fun bind(homeItem: HomeItem) {}
}

class HomeChannelShimmerViewHolder(
	private val binding: LayoutHomeChannelShimmerBinding,
) : HomeItemViewHolder(binding) {
	override fun bind(homeItem: HomeItem) {}
}