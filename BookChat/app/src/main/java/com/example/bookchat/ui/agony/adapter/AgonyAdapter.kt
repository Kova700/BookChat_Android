package com.example.bookchat.ui.agony.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyDataBinding
import com.example.bookchat.databinding.ItemAgonyDataEditingBinding
import com.example.bookchat.databinding.ItemAgonyFirstBinding
import com.example.bookchat.databinding.ItemAgonyHeaderBinding
import com.example.bookchat.ui.agony.AgonyUiState
import com.example.bookchat.ui.agony.model.AgonyListItem
import javax.inject.Inject

class AgonyAdapter @Inject constructor() :
	ListAdapter<AgonyListItem, AgonyViewHolder>(AGONY_ITEM_COMPARATOR) {

	var agonyUiState: AgonyUiState = AgonyUiState.DEFAULT
	var onFirstItemClick: (() -> Unit)? = null
	var onItemClick: ((Int) -> Unit)? = null
	var onItemSelect: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is AgonyListItem.Header -> R.layout.item_agony_header
			is AgonyListItem.FirstItem -> R.layout.item_agony_first
			is AgonyListItem.Item -> {
				if (agonyUiState.uiState == AgonyUiState.UiState.EDITING) R.layout.item_agony_data_editing
				else R.layout.item_agony_data
			}
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): AgonyViewHolder {

		when (viewType) {
			R.layout.item_agony_header -> {
				val binding: ItemAgonyHeaderBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_agony_header,
					parent, false
				)
				return AgonyHeaderItemViewHolder(binding)
			}

			R.layout.item_agony_first -> {
				val binding: ItemAgonyFirstBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_agony_first,
					parent, false
				)
				return AgonyFirstItemViewHolder(binding, onFirstItemClick)
			}

			R.layout.item_agony_data_editing -> {
				val binding: ItemAgonyDataEditingBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_agony_data_editing,
					parent, false
				)
				return AgonyDataEditingItemViewHolder(binding, onItemSelect)
			}

			else -> {
				val binding: ItemAgonyDataBinding = DataBindingUtil.inflate(
					LayoutInflater.from(parent.context), R.layout.item_agony_data,
					parent, false
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