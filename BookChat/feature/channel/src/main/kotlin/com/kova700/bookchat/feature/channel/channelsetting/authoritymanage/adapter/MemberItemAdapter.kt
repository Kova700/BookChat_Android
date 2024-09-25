package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.model.MemberItem
import com.kova700.bookchat.feature.channel.databinding.ItemChannelMemberManageBinding
import javax.inject.Inject
import com.kova700.bookchat.feature.channel.R as channelR

class MemberItemAdapter @Inject constructor() :
	ListAdapter<MemberItem, MemberItemViewHolder>(MEMBER_ITEM_COMPARATOR) {
	var onClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return channelR.layout.item_channel_member_manage
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): MemberItemViewHolder {
		val binding = ItemChannelMemberManageBinding.inflate(
			LayoutInflater.from(parent.context), parent, false
		)
		return MemberItemViewHolder(binding, onClick)
	}

	override fun onBindViewHolder(holder: MemberItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val MEMBER_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<MemberItem>() {
			override fun areItemsTheSame(oldItem: MemberItem, newItem: MemberItem) =
				oldItem.id == newItem.id

			override fun areContentsTheSame(oldItem: MemberItem, newItem: MemberItem) =
				oldItem == newItem
		}
	}
}
