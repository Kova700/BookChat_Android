package com.kova700.bookchat.feature.channel.drawer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.channel.databinding.ItemChatDrawerDataBinding
import com.kova700.bookchat.feature.channel.databinding.ItemChatDrawerHeaderBinding
import com.kova700.bookchat.feature.channel.drawer.model.ChannelDrawerItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.channel.R as channelR

class ChannelDrawerAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<ChannelDrawerItem, ChatDrawerItemViewHolder>(CHANNEL_DRAWER_ITEM_COMPARATOR) {
	var onClickUserProfile: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ChannelDrawerItem.Header -> channelR.layout.item_chat_drawer_header
			is ChannelDrawerItem.UserItem -> channelR.layout.item_chat_drawer_data
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): ChatDrawerItemViewHolder {
		when (viewType) {
			channelR.layout.item_chat_drawer_header -> {
				val binding = ItemChatDrawerHeaderBinding.inflate(
					LayoutInflater.from(parent.context),
					parent, false
				)
				return ChannelDrawerHeaderItemViewHolder(binding, bookImgSizeManager)
			}

			channelR.layout.item_chat_drawer_data -> {
				val binding = ItemChatDrawerDataBinding.inflate(
					LayoutInflater.from(parent.context),
					parent, false
				)
				return ChannelDrawerDataItemViewHolder(binding, onClickUserProfile)
			}

			else -> throw Exception("unknown ViewHolder Type")
		}
	}

	override fun onBindViewHolder(holder: ChatDrawerItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val CHANNEL_DRAWER_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChannelDrawerItem>() {
			override fun areItemsTheSame(oldItem: ChannelDrawerItem, newItem: ChannelDrawerItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: ChannelDrawerItem, newItem: ChannelDrawerItem) =
				oldItem == newItem
		}
	}
}