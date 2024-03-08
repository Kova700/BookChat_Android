package com.example.bookchat.ui.channelList.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatRoomListDataBinding
import com.example.bookchat.domain.model.Channel
import javax.inject.Inject

class ChannelListDataAdapter @Inject constructor() :
	ListAdapter<Channel, ChatRoomItemViewHolder>(CHAT_ROOM_LIST_ITEM_COMPARATOR) {
	var onItemClick: ((Int) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomItemViewHolder {
		val binding: ItemChatRoomListDataBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context), R.layout.item_chat_room_list_data,
			parent, false
		)
		return ChatRoomItemViewHolder(binding, onItemClick)
	}

	override fun onBindViewHolder(holder: ChatRoomItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	override fun getItemViewType(position: Int): Int = R.layout.item_chat_room_list_data

	companion object {
		val CHAT_ROOM_LIST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Channel>() {
			override fun areItemsTheSame(oldItem: Channel, newItem: Channel) =
				oldItem.roomId == newItem.roomId

			override fun areContentsTheSame(oldItem: Channel, newItem: Channel) =
				oldItem == newItem
		}
	}
}

class ChatRoomItemViewHolder(
	private val binding: ItemChatRoomListDataBinding,
	private val onItemClick: ((Int) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(bindingAdapterPosition)
		}
	}

	fun bind(channel: Channel) {
		binding.channel = channel
	}
}
