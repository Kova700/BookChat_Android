package com.example.bookchat.ui.channelList.adpater

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemChannelListDataBinding
import com.example.bookchat.databinding.ItemChannelListHeaderBinding
import com.example.bookchat.ui.channelList.model.ChannelListItem

sealed class ChannelListItemViewHolder(
	binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(channelListItem: ChannelListItem)
}

class ChannelListHeaderViewHolder(
	private val binding: ItemChannelListHeaderBinding
) : ChannelListItemViewHolder(binding) {
	override fun bind(channelListItem: ChannelListItem) {}
}

class ChannelListDataViewHolder(
	private val binding: ItemChannelListDataBinding,
	private val onItemClick: ((Int) -> Unit)?
) : ChannelListItemViewHolder(binding) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	override fun bind(channelListItem: ChannelListItem) {
		val item = (channelListItem as ChannelListItem.ChannelItem)
		binding.channel = item
	}

}
