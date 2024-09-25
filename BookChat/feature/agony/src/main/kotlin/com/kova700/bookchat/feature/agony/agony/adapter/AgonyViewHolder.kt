package com.kova700.bookchat.feature.agony.agony.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyDataBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyDataEditingBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyFirstBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyHeaderBinding
import com.kova700.bookchat.feature.agony.makeagony.util.getTextColorHexInt
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.image.image.loadUrl

sealed class AgonyViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(agonyListItem: AgonyListItem)
}

class AgonyHeaderItemViewHolder(
	private val binding: ItemAgonyHeaderBinding,
	private val bookImgSizeManager: BookImgSizeManager,
) : AgonyViewHolder(binding) {
	init {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
	}

	override fun bind(agonyListItem: AgonyListItem) {
		val item = (agonyListItem as AgonyListItem.Header)
		with(binding) {
			selectedBookTitleTv.text = item.bookShelfItem.book.title
			selectedBookTitleTv.isSelected = true
			selectedBookAuthorsTv.text = item.bookShelfItem.book.authorsString
			selectedBookAuthorsTv.isSelected = true
			bookImg.loadUrl(item.bookShelfItem.book.bookCoverImageUrl)
		}
	}
}

class AgonyFirstItemViewHolder(
	private val binding: ItemAgonyFirstBinding,
	private val onFirstItemClick: (() -> Unit)?,
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onFirstItemClick?.invoke()
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {}
}

class AgonyDataItemViewHolder(
	private val binding: ItemAgonyDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {
		val item = (agonyListItem as AgonyListItem.Item)
		with(binding) {
			titleAgonyFolderTv.text = item.title
			titleAgonyFolderTv.setTextColor(item.hexColorCode.getTextColorHexInt())
			backgroundAgonyFolderCv.backgroundTintList = ColorStateList.valueOf(
				Color.parseColor(item.hexColorCode.hexcolor)
			)
		}
	}
}

class AgonyDataEditingItemViewHolder(
	private val binding: ItemAgonyDataEditingBinding,
	private val onItemSelect: ((Int) -> Unit)?,
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemSelect?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {
		val item = (agonyListItem as AgonyListItem.Item)
		with(binding) {
			titleAgonyEditFolderTv.text = item.title
			agonyFolderCheckIv.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
			backgroundAgonyEditFolderCv.backgroundTintList =
				if (item.isSelected) root.context.getColorStateList(R.color.agony_color_selected)
				else root.context.getColorStateList(R.color.agony_color_white)
		}
	}
}