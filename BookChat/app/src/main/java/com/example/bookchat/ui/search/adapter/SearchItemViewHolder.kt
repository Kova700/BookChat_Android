package com.example.bookchat.ui.search.adapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemSearchBookDataBinding
import com.example.bookchat.databinding.ItemSearchBookDummyBinding
import com.example.bookchat.databinding.ItemSearchBookEmptyBinding
import com.example.bookchat.databinding.ItemSearchBookHeaderBinding
import com.example.bookchat.databinding.ItemSearchChannelDataBinding
import com.example.bookchat.databinding.ItemSearchChannelEmptyBinding
import com.example.bookchat.databinding.ItemSearchChannelHeaderBinding
import com.example.bookchat.ui.search.model.SearchResultItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DateManager
import com.example.bookchat.utils.image.loadChannelProfile

sealed class SearchItemViewHolder(
	binding: ViewDataBinding,
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
		binding.book = item
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
) : SearchItemViewHolder(binding) {
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
		val item = (searchResultItem as SearchResultItem.ChannelItem)
		binding.channel = item
		item.lastChat?.let {
			binding.lastChatDispatchTimeTv.text =
				DateManager.getFormattedAbstractDateTimeText(it.dispatchTime)
		}
		binding.channelImageIv.loadChannelProfile(
			imageUrl = item.roomImageUri,
			channelDefaultImageType = item.defaultRoomImageType
		)

		binding.roommemberCountTv.text = itemView.context.getString(
			R.string.room_member_count,
			item.roomMemberCount
		)
	}
}