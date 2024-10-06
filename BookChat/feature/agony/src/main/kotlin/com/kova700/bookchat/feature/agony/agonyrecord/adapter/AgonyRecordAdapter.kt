package com.kova700.bookchat.feature.agony.agonyrecord.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordDataBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordFirstBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordHeaderBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordPagingRetryBinding
import javax.inject.Inject
import com.kova700.bookchat.feature.agony.R as agonyR

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

	var onClickPagingRetry: (() -> Unit)? = null

	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is AgonyRecordListItem.Header -> agonyR.layout.item_agony_record_header
			is AgonyRecordListItem.FirstItem -> agonyR.layout.item_agony_record_first
			is AgonyRecordListItem.Item -> agonyR.layout.item_agony_record_data
			AgonyRecordListItem.PagingError -> agonyR.layout.item_agony_record_paging_retry
		}
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	): AgonyRecordViewHolder {

		when (viewType) {
			agonyR.layout.item_agony_record_header -> {
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

			agonyR.layout.item_agony_record_first -> {
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

			agonyR.layout.item_agony_record_data -> {
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

			agonyR.layout.item_agony_record_paging_retry -> {
				val binding = ItemAgonyRecordPagingRetryBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
				return AgonyRecordPagingErrorViewHolder(
					binding = binding,
					onClickPagingRetry = onClickPagingRetry
				)
			}

			else -> throw IllegalArgumentException("Unknown viewType: $viewType")
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