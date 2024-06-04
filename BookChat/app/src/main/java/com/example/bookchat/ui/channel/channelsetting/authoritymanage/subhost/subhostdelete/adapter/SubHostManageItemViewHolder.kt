package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemSubHostDeleteBinding
import com.example.bookchat.domain.model.User

class SubHostManageItemViewHolder(
	private val binding: ItemSubHostDeleteBinding,
	private val onClickDeleteBtn: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {
	init {
		binding.subHostDeleteChip.setOnClickListener {
			onClickDeleteBtn?.invoke(absoluteAdapterPosition)
		}
	}

	fun bind(user: User) {
		binding.user = user
	}
}