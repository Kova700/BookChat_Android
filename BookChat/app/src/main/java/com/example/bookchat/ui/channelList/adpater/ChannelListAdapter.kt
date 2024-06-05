package com.example.bookchat.ui.channelList.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChannelListDataBinding
import com.example.bookchat.databinding.ItemChannelListHeaderBinding
import com.example.bookchat.ui.channelList.model.ChannelListItem
import javax.inject.Inject

//TODO : long Click :채팅방 상단고정, 알림 끄기 설정 가능한 다이얼로그
//TODO : Swipe : 상단고정, 알림 끄기 UI 노출
class ChannelListAdapter @Inject constructor() :
	ListAdapter<ChannelListItem, ChannelListItemViewHolder>(CHANNEL_LIST_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null
	var onItemLongClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			ChannelListItem.Header -> R.layout.item_channel_list_header
			is ChannelListItem.ChannelItem -> R.layout.item_channel_list_data
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListItemViewHolder {
		when (viewType) {
			R.layout.item_channel_list_header -> {
				val binding: ItemChannelListHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context),
					R.layout.item_channel_list_header, parent, false
				)
				return ChannelListHeaderViewHolder(binding)
			}

			R.layout.item_channel_list_data -> {
				val binding: ItemChannelListDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_channel_list_data,
					parent, false
				)
				return ChannelListDataViewHolder(binding, onItemClick, onItemLongClick)
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