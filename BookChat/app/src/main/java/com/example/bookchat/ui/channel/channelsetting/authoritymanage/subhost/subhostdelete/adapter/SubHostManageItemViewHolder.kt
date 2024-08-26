package com.example.bookchat.ui.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemSubHostDeleteBinding
import com.example.bookchat.domain.model.User
import com.example.bookchat.utils.image.loadUserProfile

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
		with(binding) {
			userProfileIv.loadUserProfile(
				imageUrl = user.profileImageUrl,
				userDefaultProfileType = user.defaultProfileImageType
			)
			uesrNicknameTv.text = user.nickname.ifBlank { "(알 수 없음)" }
		}
	}
}