package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.feature.channel.databinding.ItemSubHostDeleteBinding
import javax.inject.Inject
import com.kova700.bookchat.feature.channel.R as channelR

class SubHostDeleteItemAdapter @Inject constructor() :
	ListAdapter<User, SubHostManageItemViewHolder>(SUB_HOST_ITEM_COMPARATOR) {
	var onClickDeleteBtn: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return channelR.layout.item_sub_host_delete
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): SubHostManageItemViewHolder {
		val binding = ItemSubHostDeleteBinding.inflate(
			LayoutInflater.from(parent.context),
			parent, false
		)
		return SubHostManageItemViewHolder(binding, onClickDeleteBtn)
	}

	override fun onBindViewHolder(holder: SubHostManageItemViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		private val SUB_HOST_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<User>() {
			override fun areItemsTheSame(oldItem: User, newItem: User) =
				oldItem.id == newItem.id

			override fun areContentsTheSame(oldItem: User, newItem: User) =
				oldItem == newItem
		}
	}
}