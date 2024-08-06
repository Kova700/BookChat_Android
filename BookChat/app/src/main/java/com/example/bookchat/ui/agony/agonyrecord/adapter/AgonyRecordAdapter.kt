package com.example.bookchat.ui.agony.agonyrecord.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bookchat.R
import com.example.bookchat.databinding.ItemAgonyRecordDataBinding
import com.example.bookchat.databinding.ItemAgonyRecordFirstBinding
import com.example.bookchat.databinding.ItemAgonyRecordHeaderBinding
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem
import javax.inject.Inject

class AgonyRecordAdapter @Inject constructor() :
	ListAdapter<AgonyRecordListItem, AgonyRecordViewHolder>(AGONY_RECORD_ITEM_COMPARATOR) {

	var onHeaderEditBtnClick: (() -> Unit)? = null

	var onFirstItemClick: (() -> Unit)? = null
	var onFirstItemEditCancelBtnClick: (() -> Unit)? = null
	var onFirstItemEditFinishBtnClick: (() -> Unit)? = null

	var onItemClick: ((Int) -> Unit)? = null
	var onItemSwipe: ((Int, Boolean) -> Unit)? = null
	var onItemEditCancelBtnClick: ((Int) -> Unit)? = null
	var onItemEditFinishBtnClick: ((Int) -> Unit)? = null
	var onItemDeleteBtnClick: ((Int) -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is AgonyRecordListItem.Header -> R.layout.item_agony_record_header
			is AgonyRecordListItem.FirstItem -> R.layout.item_agony_record_first
			is AgonyRecordListItem.Item -> R.layout.item_agony_record_data
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): AgonyRecordViewHolder {

		when (viewType) {
			R.layout.item_agony_record_header -> {
				val binding = ItemAgonyRecordHeaderBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				return AgonyRecordHeaderViewHolder(
					binding = binding,
					onHeaderEditBtnClick = onHeaderEditBtnClick
				)
			}

			R.layout.item_agony_record_first -> {
				val binding = ItemAgonyRecordFirstBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				return AgonyRecordFirstItemViewHolder(
					binding = binding,
					onFirstItemClick = onFirstItemClick,
					onFirstItemEditCancelBtnClick = onFirstItemEditCancelBtnClick,
					onFirstItemEditFinishBtnClick = onFirstItemEditFinishBtnClick,
				)
			}

			else -> {
				val binding = ItemAgonyRecordDataBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				return AgonyRecordItemViewHolder(
					binding = binding,
					onItemClick = onItemClick,
					onItemSwipe = onItemSwipe,
					onItemEditCancelBtnClick = onItemEditCancelBtnClick,
					onItemEditFinishBtnClick = onItemEditFinishBtnClick,
					onItemDeleteBtnClick = onItemDeleteBtnClick,
				)
			}
		}
	}

	override fun onBindViewHolder(holder: AgonyRecordViewHolder, position: Int) {
		holder.bind(getItem(position))
	}

	companion object {
		val AGONY_RECORD_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AgonyRecordListItem>() {
			override fun areItemsTheSame(oldItem: AgonyRecordListItem, newItem: AgonyRecordListItem) =
				oldItem.getCategoryId() == newItem.getCategoryId()

			override fun areContentsTheSame(oldItem: AgonyRecordListItem, newItem: AgonyRecordListItem) =
				oldItem == newItem
		}
	}
}