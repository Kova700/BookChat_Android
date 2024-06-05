package com.example.bookchat.ui.agony.agonyrecord.adapter

import android.text.Editable
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.databinding.ItemAgonyRecordDataBinding
import com.example.bookchat.databinding.ItemAgonyRecordFirstBinding
import com.example.bookchat.databinding.ItemAgonyRecordHeaderBinding
import com.example.bookchat.ui.agony.agonyrecord.AgonyRecordSwipeHelper
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem

sealed class AgonyRecordViewHolder(
	binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(agonyRecordListItem: AgonyRecordListItem)
}

class AgonyRecordHeaderViewHolder(
	private val binding: ItemAgonyRecordHeaderBinding,
	private val onHeaderEditBtnClick: (() -> Unit)?,
) : AgonyRecordViewHolder(binding) {

	init {
		binding.agonyTitleEidtBtn.setOnClickListener {
			onHeaderEditBtnClick?.invoke()
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.Header
		binding.agony = item.agony
	}
}

class AgonyRecordFirstItemViewHolder(
	private val binding: ItemAgonyRecordFirstBinding,
	private val onFirstItemClick: (() -> Unit)?,
	private val onFirstItemEditCancelBtnClick: (() -> Unit)?,
	private val onFirstItemEditFinishBtnClick: (() -> Unit)?,
) : AgonyRecordViewHolder(binding) {

	init {
		binding.agonyRecordFirstItemCv.setOnClickListener {
			onFirstItemClick?.invoke()
		}
		binding.editLayout.agonyRecordFirstItemEditCancelBtn.setOnClickListener {
			onFirstItemEditCancelBtnClick?.invoke()
		}
		binding.editLayout.agonyRecordFirstItemEditSubmitBtn.setOnClickListener {
			onFirstItemEditFinishBtnClick?.invoke()
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.FirstItem
		binding.firstItem = item
		binding.editLayout.agonyRecordFirstItemTitleEt

		if (item.state is AgonyRecordListItem.ItemState.Editing) {
			with(binding.editLayout) {
				agonyRecordFirstItemTitleEt.setText(item.state.titleBeingEdited)
				agonyRecordFirstItemContentEt.setText(item.state.contentBeingEdited)
				agonyRecordFirstItemTitleEt.addTextChangedListener { text: Editable? ->
					item.state.titleBeingEdited = text.toString()
				}
				agonyRecordFirstItemContentEt.addTextChangedListener { text: Editable? ->
					item.state.contentBeingEdited = text.toString()
				}
			}

		}

	}
}

class AgonyRecordItemViewHolder(
	private val binding: ItemAgonyRecordDataBinding,
	private val onItemClick: ((Int) -> Unit)?,
	private val onItemSwipe: ((Int, Boolean) -> Unit)?,
	private val onItemEditCancelBtnClick: ((Int) -> Unit)?,
	private val onItemEditFinishBtnClick: ((Int) -> Unit)?,
	private val onItemDeleteBtnClick: ((Int) -> Unit)?,
) : AgonyRecordViewHolder(binding) {

	private var isSwiped: Boolean = false

	init {
		binding.swipeView.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
		binding.editLayout.agonyRecordEditCancelBtn.setOnClickListener {
			onItemEditCancelBtnClick?.invoke(absoluteAdapterPosition)
		}
		binding.editLayout.agonyRecordEditFinishTv.setOnClickListener {
			onItemEditFinishBtnClick?.invoke(absoluteAdapterPosition)
		}
		binding.swipeBackground.background.setOnClickListener {
			onItemDeleteBtnClick?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.Item
		binding.record = item

		when (item.state) {
			is AgonyRecordListItem.ItemState.Editing -> {
				with(binding.editLayout) {
					agonyRecordEditTitleEt.setText(item.state.titleBeingEdited)
					agonyRecordEditContentEt.setText(item.state.contentBeingEdited)
					agonyRecordEditTitleEt.addTextChangedListener { text: Editable? ->
						item.state.titleBeingEdited = text.toString()
					}
					agonyRecordEditContentEt.addTextChangedListener { text: Editable? ->
						item.state.contentBeingEdited = text.toString()
					}
				}
			}

			is AgonyRecordListItem.ItemState.Success -> {
				isSwiped = item.state.isSwiped
				setViewHolderSwipeState(binding.swipeView, item.state.isSwiped)
			}

			else -> {}
		}
	}

	fun setSwiped(isSwiped: Boolean) {
		if (this.isSwiped == isSwiped) return

		this.isSwiped = isSwiped
		onItemSwipe?.invoke(absoluteAdapterPosition, isSwiped)
	}

	fun getSwiped(): Boolean {
		return this.isSwiped
	}

	private fun setViewHolderSwipeState(swipeableView: View, isSwiped: Boolean) {
		if (isSwiped.not()) {
			swipeableView.translationX = 0f
			return
		}
		swipeableView.post {
			swipeableView.translationX =
				swipeableView.measuredWidth.toFloat() * AgonyRecordSwipeHelper.SWIPE_VIEW_PERCENT
		}
	}
}
