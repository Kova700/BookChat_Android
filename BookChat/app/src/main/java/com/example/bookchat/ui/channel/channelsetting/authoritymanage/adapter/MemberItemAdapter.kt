package com.example.bookchat.ui.channel.channelsetting.authoritymanage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChannelMemberManageBinding
import com.example.bookchat.ui.channel.channelsetting.authoritymanage.model.MemberItem
import javax.inject.Inject

class MemberItemAdapter @Inject constructor() :
	ListAdapter<MemberItem, MemberItemViewHolder>(MEMBER_ITEM_COMPARATOR) {
	var onClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return R.layout.item_channel_member_manage
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): MemberItemViewHolder {
		val binding = ItemChannelMemberManageBinding.inflate(
			LayoutInflater.from(parent.context),
			parent, false
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
