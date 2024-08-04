package com.example.bookchat.ui.home.channel.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.databinding.ItemHomeChannelBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.home.adapter.HomeChannelViewHolder
import javax.inject.Inject
//
//class HomeChannelAdapter @Inject constructor() :
//	ListAdapter<Channel, HomeChannelViewHolder>(CHANNEL_COMPARATOR) {
//	var onItemClick: ((Int) -> Unit)? = null
//
//	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeChannelViewHolder {
//		val binding = ItemHomeChannelBinding.inflate(
//			LayoutInflater.from(parent.context),
//			parent,
//			false
//		)
//		return HomeChannelViewHolder(binding, onItemClick)
//	}
//
//	override fun onBindViewHolder(holder: HomeChannelViewHolder, position: Int) {
//		holder.bind(getItem(position))
//	}
//
//	companion object {
//		val CHANNEL_COMPARATOR = object : DiffUtil.ItemCallback<Channel>() {
//			override fun areItemsTheSame(oldItem: Channel, newItem: Channel) =
//				oldItem.roomId == newItem.roomId
//
//			override fun areContentsTheSame(oldItem: Channel, newItem: Channel) =
//				oldItem == newItem
//		}
//	}
//}