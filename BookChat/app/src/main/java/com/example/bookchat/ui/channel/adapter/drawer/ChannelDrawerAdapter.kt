package com.example.bookchat.ui.channel.adapter.drawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatDrawerDataBinding
import com.example.bookchat.databinding.ItemChatDrawerHeaderBinding
import com.example.bookchat.ui.channel.model.drawer.ChannelDrawerItem
import com.example.bookchat.utils.BookImgSizeManager
import javax.inject.Inject

class ChannelDrawerAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager
) : ListAdapter<ChannelDrawerItem, ChatDrawerItemViewHolder>(CHANNEL_DRAWER_ITEM_COMPARATOR) {

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ChannelDrawerItem.Header -> R.layout.item_chat_drawer_header
			is ChannelDrawerItem.UserItem -> R.layout.item_chat_drawer_data
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ChatDrawerItemViewHolder {
		when (viewType) {
			R.layout.item_chat_drawer_header -> {
				val binding: ItemChatDrawerHeaderBinding =
					DataBindingUtil.inflate(
						LayoutInflater.from(parent.context), R.layout.item_chat_drawer_header,
						parent, false
					)
				return ChannelDrawerHeaderItemViewHolder(binding, bookImgSizeManager)
			}

			R.layout.item_chat_drawer_data -> {
				val binding: ItemChatDrawerDataBinding =
					DataBindingUtil.inflate(
						LayoutInflater.from(parent.context), R.layout.item_chat_drawer_data,
						parent, false
					)
				return ChannelDrawerDataItemViewHolder(binding)
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