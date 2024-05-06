package com.example.bookchat.ui.channel.adapter.drawer

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemChatDrawerDataBinding
import com.example.bookchat.databinding.ItemChatDrawerHeaderBinding
import com.example.bookchat.ui.channel.model.drawer.ChannelDrawerItem
import com.example.bookchat.utils.BookImgSizeManager

sealed class ChatDrawerItemViewHolder(
	binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(channelDrawerItem: ChannelDrawerItem)
}

class ChannelDrawerHeaderItemViewHolder(
	private val binding: ItemChatDrawerHeaderBinding,
	private val bookImgSizeManager: BookImgSizeManager
) : ChatDrawerItemViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(channelDrawerItem: ChannelDrawerItem) {
		val item = (channelDrawerItem as ChannelDrawerItem.Header)
		binding.headerItem = item
	}
}

class ChannelDrawerDataItemViewHolder(
	private val binding: ItemChatDrawerDataBinding
) : ChatDrawerItemViewHolder(binding) {

	override fun bind(channelDrawerItem: ChannelDrawerItem) {
		val item = (channelDrawerItem as ChannelDrawerItem.UserItem)
		binding.userItem = item
	}
}