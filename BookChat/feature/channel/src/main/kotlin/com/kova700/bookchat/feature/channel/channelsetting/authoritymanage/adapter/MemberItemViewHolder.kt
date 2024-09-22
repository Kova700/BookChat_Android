package com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.feature.channel.channelsetting.authoritymanage.model.MemberItem
import com.kova700.bookchat.feature.channel.databinding.ItemChannelMemberManageBinding
import com.kova700.bookchat.util.image.image.loadUserProfile

class MemberItemViewHolder(
	private val binding: ItemChannelMemberManageBinding,
	private val onClick: ((Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {
	init {
		binding.root.setOnClickListener {
			onClick?.invoke(absoluteAdapterPosition)
		}
		binding.memberSelectRb.setOnClickListener {
			onClick?.invoke(absoluteAdapterPosition)
		}
	}

	fun bind(item: MemberItem) {
		with(binding) {
			memberSelectRb.isChecked = item.isSelected
			subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
			userProfileIv.loadUserProfile(
				imageUrl = item.profileImageUrl,
				userDefaultProfileType = item.defaultProfileImageType
			)
			uesrNicknameTv.text = item.nickname.ifBlank { "(알 수 없음)" }
		}
	}
}