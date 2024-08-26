package com.example.bookchat.ui.channel.drawer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatDrawerDataBinding
import com.example.bookchat.databinding.ItemChatDrawerHeaderBinding
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.image.loadUserProfile

sealed class ChatDrawerItemViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(channelDrawerItem: ChannelDrawerItem)
}

class ChannelDrawerHeaderItemViewHolder(
	private val binding: ItemChatDrawerHeaderBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : ChatDrawerItemViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(channelDrawerItem: ChannelDrawerItem) {
		val item = (channelDrawerItem as ChannelDrawerItem.Header)
		with(binding) {
			bookImg.loadUrl(item.bookCoverImageUrl)
			bookTitleTv.text = item.bookTitle
			bookAuthorTv.text = item.bookAuthors
			channelTitleTv.text = item.roomName
		}
	}
}

class ChannelDrawerDataItemViewHolder(
	private val binding: ItemChatDrawerDataBinding,
	private val onClickUserProfile: ((Int) -> Unit)?,
) : ChatDrawerItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onClickUserProfile?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(channelDrawerItem: ChannelDrawerItem) {
		val item = (channelDrawerItem as ChannelDrawerItem.UserItem)
		with(binding) {
			userProfileIv.loadUserProfile(
				imageUrl = item.profileImageUrl,
				userDefaultProfileType = item.defaultProfileImageType
			)
			userNicknameTv.text = item.nickname
		}
		setClientItemView(item)
		setAdminItemView(item)
	}

	private fun setClientItemView(item: ChannelDrawerItem.UserItem) {
		if (item.isClientItem) {
			with(binding) {
				val drawable =
					ContextCompat.getDrawable(root.context, R.drawable.my_profile_text_icon)
				userNicknameTv.setCompoundDrawablesWithIntrinsicBounds(
					drawable, null, null, null
				)
				userNicknameTv.typeface =
					ResourcesCompat.getFont(binding.root.context, R.font.notosanskr_bold)
			}
			return
		}

		with(binding) {
			userNicknameTv.setCompoundDrawablesWithIntrinsicBounds(
				null, null, null, null
			)
			userNicknameTv.typeface =
				ResourcesCompat.getFont(binding.root.context, R.font.notosanskr_regular)
		}
	}

	private fun setAdminItemView(item: ChannelDrawerItem.UserItem) {
		with(binding) {
			hostCrown.visibility = if (item.isTargetUserHost) View.VISIBLE else View.GONE
			subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
		}
	}
}