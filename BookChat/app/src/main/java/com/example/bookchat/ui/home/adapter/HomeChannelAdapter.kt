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
	private val onItemClick: ((Int) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	fun bind(channel: Channel) {
		binding.channel = channel
		binding.uncheckedChatCountTv.text = if (channel.isExistNewChat) "New+" else ""
		binding.muteChannelIcon.visibility =
			if ((channel.notificationFlag.not())
				&& channel.isAvailableChannel.not()
			) View.VISIBLE else View.GONE
		binding.topPinChannelIcon.visibility =
			if ((channel.topPinNum != 0)
				&& channel.isAvailableChannel.not()
			) View.VISIBLE else View.GONE
		binding.unavailableChannelStateGroup.visibility =
			if (channel.isAvailableChannel.not()) View.VISIBLE else View.GONE
	}
}