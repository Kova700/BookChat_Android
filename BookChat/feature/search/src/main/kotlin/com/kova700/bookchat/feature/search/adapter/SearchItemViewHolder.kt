package com.kova700.bookchat.feature.search.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookDummyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchBookHeaderBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelDataBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelEmptyBinding
import com.kova700.bookchat.feature.search.databinding.ItemSearchChannelHeaderBinding
import com.kova700.bookchat.feature.search.model.SearchResultItem
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.date.getFormattedAbstractDateTimeText
import com.kova700.bookchat.util.image.image.loadChannelProfile
import com.kova700.bookchat.util.image.image.loadUrl

sealed class SearchItemViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(searchResultItem: SearchResultItem)
}

class BookHeaderViewHolder(
	private val binding: ItemSearchBookHeaderBinding,
	private val onBookHeaderBtnClick: (() -> Unit)?,
) : SearchItemViewHolder(binding) {
	init {
		binding.searchBookHeaderIb.setOnClickListener {
			onBookHeaderBtnClick?.invoke()
		}
	}

	override fun bind(searchResultItem: SearchResultItem) {}
}

class BookEmptyViewHolder(
	private val binding: ItemSearchBookEmptyBinding,
) : SearchItemViewHolder(binding) {
	override fun bind(searchResultItem: SearchResultItem) {}
}

class BookItemViewHolder(
	private val binding: ItemSearchBookDataBinding,
	private val bookImgSizeManager: BookImgSizeManager,
	private val onItemClick: ((Int) -> Unit)?,
) : SearchItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(searchResultItem: SearchResultItem) {
		val item = (searchResultItem as SearchResultItem.BookItem)
		binding.bookImg.loadUrl(item.bookCoverImageUrl)
	}
}

class BookDummyViewHolder(
	private val binding: ItemSearchBookDummyBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : SearchItemViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.flexBoxDummyBookLayout)
	}

	override fun bind(searchResultItem: SearchResultItem) {}
}

class ChannelHeaderViewHolder(
	private val binding: ItemSearchChannelHeaderBinding,
	private val onChannelHeaderBtnClick: (() -> Unit)?,
) : SearchItemViewHolder(binding) {
	init {
		binding.searchChannelHeaderIb.setOnClickListener {
			onChannelHeaderBtnClick?.invoke()
		}
	}

	override fun bind(searchResultItem: SearchResultItem) {}
}

class ChannelEmptyViewHolder(
	private val binding: ItemSearchChannelEmptyBinding,
	private val onClickMakeChannelBtn: (() -> Unit)?,
) : SearchItemViewHolder(binding) {
	init {
		binding.makeChannelBtn.setOnClickListener { onClickMakeChannelBtn?.invoke() }
	}

	override fun bind(searchResultItem: SearchResultItem) {}
}

class ChannelItemViewHolder(
	private val binding: ItemSearchChannelDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
) : SearchItemViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(searchResultItem: SearchResultItem) {
		initViewState(searchResultItem)
	}

	private fun initViewState(searchResultItem: SearchResultItem) {
		val item = (searchResultItem as SearchResultItem.ChannelItem)
		with(binding) {
			item.lastChat?.let {
				lastChatDispatchTimeTv.text =
					getFormattedAbstractDateTimeText(it.dispatchTime)
			}
			channelImageIv.loadChannelProfile(
				imageUrl = item.roomImageUri,
				channelDefaultImageType = item.defaultRoomImageType
			)

			roommemberCountTv.text = itemView.context.getString(
				R.string.room_member_count,
				item.roomMemberCount
			)
			channelTitleTv.text = item.roomName
			channelTagsTv.text = item.tagsString
		}
	}
}