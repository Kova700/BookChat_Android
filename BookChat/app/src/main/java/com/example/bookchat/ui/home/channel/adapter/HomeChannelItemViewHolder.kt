package com.example.bookchat.ui.home.channel.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemHomeChannelBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.utils.getFormattedDetailDateTimeText
import com.example.bookchat.utils.image.loadChannelProfile
//
//class HomeChannelItemViewHolder(
//	private val binding: ItemHomeChannelBinding,
//	private val onItemClick: ((Int) -> Unit)?,
//) : RecyclerView.ViewHolder(binding.root) {
//
//	init {
//		binding.root.setOnClickListener {
//			onItemClick?.invoke(bindingAdapterPosition)
//		}
//	}
//
//	fun bind(channel: Channel) {
//		with(binding) {
//			binding.channel = channel
//			dispatchTimeTv.text =
//				channel.lastChat?.dispatchTime?.let { getFormattedDetailDateTimeText(it) }
//			uncheckedChatCountTv.text = if (channel.isExistNewChat) "New+" else ""
//			muteChannelIcon.visibility =
//				if ((channel.notificationFlag.not()) && channel.isAvailableChannel) View.VISIBLE else View.GONE
//			topPinChannelIcon.visibility =
//				if ((channel.isTopPined) && channel.isAvailableChannel) View.VISIBLE else View.GONE
//			unavailableChannelStateGroup.visibility =
//				if (channel.isAvailableChannel.not()) View.VISIBLE else View.GONE
//			channelImageIv.loadChannelProfile(
//				imageUrl = channel.roomImageUri,
//				channelDefaultImageType = channel.defaultRoomImageType
//			)
//		}
//	}
//}