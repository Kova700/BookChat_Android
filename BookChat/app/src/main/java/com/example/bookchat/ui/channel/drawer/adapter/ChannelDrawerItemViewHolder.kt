package com.example.bookchat.ui.channel.drawer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemChatDrawerDataBinding
import com.example.bookchat.databinding.ItemChatDrawerHeaderBinding
import com.example.bookchat.ui.channel.drawer.model.ChannelDrawerItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.image.loadUserProfile

sealed class ChatDrawerItemViewHolder(
	binding: ViewDataBinding,
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
		binding.headerItem = item
		binding.bookImg.loadUrl(item.bookCoverImageUrl)
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
		binding.userItem = item
		binding.userProfileIv.loadUserProfile(
			imageUrl = item.profileImageUrl,
			userDefaultProfileType = item.defaultProfileImageType
		)
		setClientItemView(item)
		setAdminItemView(item)
	}

	private fun setClientItemView(item: ChannelDrawerItem.UserItem) {
		if (item.isClientItem) {
			val drawable =
				ContextCompat.getDrawable(binding.root.context, R.drawable.my_profile_text_icon)
			binding.userNicknameTv.setCompoundDrawablesWithIntrinsicBounds(
				drawable, null, null, null
			)
			binding.userNicknameTv.typeface =
				ResourcesCompat.getFont(binding.root.context, R.font.notosanskr_bold)
			return
		}

		binding.userNicknameTv.setCompoundDrawablesWithIntrinsicBounds(
			null, null, null, null
		)
		binding.userNicknameTv.typeface =
			ResourcesCompat.getFont(binding.root.context, R.font.notosanskr_regular)
	}

	private fun setAdminItemView(item: ChannelDrawerItem.UserItem) {
		binding.hostCrown.visibility = if (item.isTargetUserHost) View.VISIBLE else View.GONE
		binding.subHostCrown.visibility = if (item.isTargetUserSubHost) View.VISIBLE else View.GONE
	}
}