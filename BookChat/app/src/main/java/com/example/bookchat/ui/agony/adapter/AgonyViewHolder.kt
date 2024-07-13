package com.example.bookchat.ui.agony.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyDataBinding
import com.example.bookchat.databinding.ItemAgonyDataEditingBinding
import com.example.bookchat.databinding.ItemAgonyFirstBinding
import com.example.bookchat.databinding.ItemAgonyHeaderBinding
import com.example.bookchat.ui.agony.makeagony.util.getTextColorHexInt
import com.example.bookchat.ui.agony.model.AgonyListItem
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.image.loadUrl

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
		binding.bookShelfItem = (agonyListItem as AgonyListItem.Header).bookShelfItem
		binding.bookImg.loadUrl(binding.bookShelfItem?.book?.bookCoverImageUrl)
		binding.selectedBookTitleTv.isSelected = true
		binding.selectedBookAuthorsTv.isSelected = true
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
		binding.agonyListItem = (agonyListItem as AgonyListItem.Item)
		binding.titleAgonyFolderTv.setTextColor(agonyListItem.hexColorCode.getTextColorHexInt())
		binding.backgroundAgonyFolderCv.backgroundTintList = ColorStateList.valueOf(
			Color.parseColor(agonyListItem.hexColorCode.hexcolor)
		)
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
		binding.agonyListItem = item
		binding.agonyFolderCheckIv.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
		binding.backgroundAgonyEditFolderCv.backgroundTintList =
			if (item.isSelected) binding.root.context.getColorStateList(R.color.agony_color_selected)
			else binding.root.context.getColorStateList(R.color.agony_color_white)
	}
}