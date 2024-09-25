package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.subhost.subhostdelete.adapter

import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.feature.channel.databinding.ItemSubHostDeleteBinding
import com.kova700.bookchat.util.image.image.loadUserProfile

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