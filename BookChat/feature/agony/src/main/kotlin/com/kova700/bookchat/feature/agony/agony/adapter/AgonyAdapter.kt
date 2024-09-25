package com.kova700.bookchat.feature.agony.agony.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.agony.agony.AgonyUiState
import com.kova700.bookchat.feature.agony.agony.model.AgonyListItem
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyDataBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyDataEditingBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyFirstBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyHeaderBinding
import com.kova700.bookchat.util.book.BookImgSizeManager
import javax.inject.Inject
import com.kova700.bookchat.feature.agony.R as agonyR

class AgonyAdapter @Inject constructor(
	private val bookImgSizeManager: BookImgSizeManager,
) : ListAdapter<AgonyListItem, AgonyViewHolder>(AGONY_ITEM_COMPARATOR) {

	var agonyUiState: AgonyUiState = AgonyUiState.DEFAULT
	var onFirstItemClick: (() -> Unit)? = null
	var onItemClick: ((Int) -> Unit)? = null
	var onItemSelect: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is AgonyListItem.Header -> agonyR.layout.item_agony_header
			is AgonyListItem.FirstItem -> agonyR.layout.item_agony_first
			is AgonyListItem.Item -> {
				if (agonyUiState.uiState == AgonyUiState.UiState.EDITING) agonyR.layout.item_agony_data_editing
				else agonyR.layout.item_agony_data
			}
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): AgonyViewHolder {

		when (viewType) {
			agonyR.layout.item_agony_header -> {
				val binding = ItemAgonyHeaderBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return AgonyHeaderItemViewHolder(binding, bookImgSizeManager)
			}

			agonyR.layout.item_agony_first -> {
				val binding = ItemAgonyFirstBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return AgonyFirstItemViewHolder(binding, onFirstItemClick)
			}

			agonyR.layout.item_agony_data_editing -> {
				val binding = ItemAgonyDataEditingBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return AgonyDataEditingItemViewHolder(binding, onItemSelect)
			}

			else -> {
				val binding = ItemAgonyDataBinding.inflate(
					LayoutInflater.from(parent.context), parent, false
				)
				return AgonyDataItemViewHolder(binding, onItemClick)
			}
		}
	}

	override fun onBindViewHolder(holder: AgonyViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val AGONY_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyListItem>() {
			override fun areItemsTheSame(oldItem: AgonyListItem, newItem: AgonyListItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: AgonyListItem, newItem: AgonyListItem) =
				oldItem == newItem
		}
	}
}