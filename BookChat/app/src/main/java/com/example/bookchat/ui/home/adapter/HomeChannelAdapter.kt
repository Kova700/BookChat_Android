package com.example.bookchat.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemHomeChannelItemBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.utils.getFormattedDetailDateTimeText
import com.example.bookchat.utils.image.loadChannelProfile
import javax.inject.Inject

class HomeChannelAdapter @Inject constructor() :
	ListAdapter<Channel, HomeChannelItemViewHolder>(CHANNEL_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeChannelItemViewHolder {
		val binding: ItemHomeChannelItemBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context),
			R.layout.item_home_channel_item, parent, false
		)
		return HomeChannelItemViewHolder(binding, onItemClick)
	}

	override fun onBindViewHolder(holder: HomeChannelItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val CHANNEL_COMPARATOR = object : DiffUtil.ItemCallback<Channel>() {
			override fun areItemsTheSame(oldItem: Channel, newItem: Channel) =
				oldItem.roomId == newItem.roomId

			override fun areContentsTheSame(oldItem: Channel, newItem: Channel) =
				oldItem == newItem
		}
	}
}

class HomeChannelItemViewHolder(
	private val binding: ItemHomeChannelItemBinding,
	private val onItemClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	fun bind(channel: Channel) {
		with(binding) {
			binding.channel = channel
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
		}
	}
}