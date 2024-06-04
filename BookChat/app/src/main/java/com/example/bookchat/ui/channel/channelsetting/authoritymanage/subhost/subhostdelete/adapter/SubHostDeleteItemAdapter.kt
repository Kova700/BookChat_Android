package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSubHostDeleteBinding
import com.example.bookchat.domain.model.User
import javax.inject.Inject

class SubHostDeleteItemAdapter @Inject constructor() :
	ListAdapter<User, SubHostManageItemViewHolder>(SUB_HOST_ITEM_COMPARATOR) {
	var onClickDeleteBtn: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return R.layout.item_sub_host_delete
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): SubHostManageItemViewHolder {
		val binding: ItemSubHostDeleteBinding = DataBindingUtil.inflate(
			LayoutInflater.from(parent.context),
			R.layout.item_sub_host_delete,
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