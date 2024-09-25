package com.kova700.bookchat.feature.channellist.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.channellist.databinding.ItemChannelListDataBinding
import com.kova700.bookchat.feature.channellist.databinding.ItemChannelListHeaderBinding
import com.kova700.bookchat.feature.channellist.model.ChannelListItem
import javax.inject.Inject
import com.kova700.bookchat.feature.channellist.R as clR

class ChannelListAdapter @Inject constructor() :
	ListAdapter<ChannelListItem, ChannelListItemViewHolder>(CHANNEL_LIST_ITEM_COMPARATOR) {
	var onSwipe: ((Int, Boolean) -> Unit)? = null
	var onClick: ((Int) -> Unit)? = null
	var onLongClick: ((Int) -> Unit)? = null
	var onClickMuteRelatedBtn: ((Int) -> Unit)? = null
	var onClickTopPinRelatedBtn: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			ChannelListItem.Header -> clR.layout.item_channel_list_header
			is ChannelListItem.ChannelItem -> clR.layout.item_channel_list_data
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListItemViewHolder {
		when (viewType) {
			clR.layout.item_channel_list_header -> {
				val binding = ItemChannelListHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ChannelListHeaderViewHolder(binding)
			}

			clR.layout.item_channel_list_data -> {
				val binding = ItemChannelListDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return ChannelListDataViewHolder(
					binding = binding,
					onItemSwipe = onSwipe,
					onItemClick = onClick,
					onItemLongClick = onLongClick,
					onItemMuteClick = onClickMuteRelatedBtn,
					onItemTopPinClick = onClickTopPinRelatedBtn,
				)
			}

			else -> throw Exception("unknown ViewHolder Type")
		}
	}

	override fun onBindViewHolder(holder: ChannelListItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val CHANNEL_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<ChannelListItem>() {
			override fun areItemsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: ChannelListItem, newItem: ChannelListItem) =
				oldItem == newItem
		}
	}
}