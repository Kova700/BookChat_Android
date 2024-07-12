package com.example.bookchat.ui.channelList.adpater

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChannelListDataBinding
import com.example.bookchat.databinding.ItemChannelListHeaderBinding
import com.example.bookchat.ui.channelList.ChannelListIItemSwipeHelper.Companion.CHANNEL_LIST_ITEM_SWIPE_VIEW_PERCENT
import com.example.bookchat.ui.channelList.model.ChannelListItem
import com.example.bookchat.utils.image.loadChannelProfile

sealed class ChannelListItemViewHolder(
	binding: ViewDataBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(channelListItem: ChannelListItem)
}

class ChannelListHeaderViewHolder(
	private val binding: ItemChannelListHeaderBinding,
) : ChannelListItemViewHolder(binding) {
	override fun bind(channelListItem: ChannelListItem) {}
}

class ChannelListDataViewHolder(
	private val binding: ItemChannelListDataBinding,
	private val onItemSwipe: ((Int, Boolean) -> Unit)?,
	private val onItemClick: ((Int) -> Unit)?,
	private val onItemLongClick: ((Int) -> Unit)?,
	private val onItemMuteClick: ((Int) -> Unit)?,
	private val onItemTopPinClick: ((Int) -> Unit)?,
) : ChannelListItemViewHolder(binding) {

	/** ChannelListIItemSwipeHelper와 함께 사용함에 있어서
	 * ViewModel의 isSwiped 상태 업데이트가 예상보다 지연되어,
	 * ViewHolder 내에 가변 프로퍼티를 사용 */
	private var isSwiped: Boolean = false

	init {
		binding.channelListSwipeView.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
		binding.channelListSwipeView.setOnLongClickListener {
			onItemLongClick?.invoke(absoluteAdapterPosition)
			true
		}
		binding.channelListSwipeBackground.channelMuteBtn.setOnClickListener {
			onItemMuteClick?.invoke(absoluteAdapterPosition)
		}
		binding.channelListSwipeBackground.channelTopPinBtn.setOnClickListener {
			onItemTopPinClick?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(channelListItem: ChannelListItem) {
		val item = (channelListItem as ChannelListItem.ChannelItem)
		isSwiped = item.isSwiped
		setViewHolderSwipeState(binding.channelListSwipeView, item.isSwiped)
		setViewState(item)
	}

	fun setViewState(channelListItem: ChannelListItem) {
		val item = (channelListItem as ChannelListItem.ChannelItem)
		with(binding) {
			channel = item
			uncheckedChatCountTv.text = if (item.isExistNewChat) "New+" else ""
			muteChannelIcon.visibility =
				if ((item.notificationFlag.not()) && item.isAvailableChannel) View.VISIBLE else View.GONE
			topPinChannelIcon.visibility =
				if ((item.isTopPined) && item.isAvailableChannel) View.VISIBLE else View.GONE
			unavailableChannelStateGroup.visibility =
				if (item.isAvailableChannel.not()) View.VISIBLE else View.GONE
			channelListSwipeBackground.channelMuteBtn.setIconResource(
				if (channelListItem.notificationFlag) R.drawable.mute_channel_icon
				else R.drawable.un_mute_channel_icon
			)
			channelListSwipeBackground.channelTopPinBtn.setIconResource(
				if (channelListItem.isTopPined) R.drawable.un_top_pin_channel_icon
				else R.drawable.top_pin_channel_icon
			)
			channelImgIv.loadChannelProfile(
				imageUrl = item.roomImageUri,
				channelDefaultImageType = item.defaultRoomImageType
			)
		}
	}

	fun setSwiped(isSwiped: Boolean) {
		if (this.isSwiped == isSwiped) return

		this.isSwiped = isSwiped
		onItemSwipe?.invoke(absoluteAdapterPosition, isSwiped)
	}

	fun getSwiped(): Boolean {
		return this.isSwiped
	}

	private fun setViewHolderSwipeState(swipeableView: View, isSwiped: Boolean) {
		if (isSwiped.not()) {
			swipeableView.translationX = 0f
			return
		}
		swipeableView.post {
			swipeableView.translationX =
				swipeableView.measuredWidth.toFloat() * CHANNEL_LIST_ITEM_SWIPE_VIEW_PERCENT
		}
	}
}
