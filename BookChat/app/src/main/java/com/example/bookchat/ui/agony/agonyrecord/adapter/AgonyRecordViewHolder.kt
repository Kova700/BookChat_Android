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
	binding: ViewDataBinding,
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
		setViewState(item.state)

	}

	private fun setViewState(state: AgonyRecordListItem.ItemState) {
		binding.progressbar.visibility =
			if (state is AgonyRecordListItem.ItemState.Loading) View.VISIBLE else View.GONE
		binding.editLayout.root.visibility =
			if (state is AgonyRecordListItem.ItemState.Editing) View.VISIBLE else View.INVISIBLE
		binding.agonyRecordFirstItemCv.visibility =
			if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.INVISIBLE

		if (state is AgonyRecordListItem.ItemState.Editing) {
			with(binding.editLayout) {
				agonyRecordFirstItemTitleEt.setText(state.titleBeingEdited)
				agonyRecordFirstItemContentEt.setText(state.contentBeingEdited)
				agonyRecordFirstItemTitleEt.addTextChangedListener { text: Editable? ->
					state.titleBeingEdited = text.toString()
				}
				agonyRecordFirstItemContentEt.addTextChangedListener { text: Editable? ->
					state.contentBeingEdited = text.toString()
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

	/** AgonyRecordSwipeHelper와 함께 사용함에 있어서
	 * ViewModel의 isSwiped 상태 업데이트가 예상보다 지연되어,
	 * ViewHolder 내에 가변 프로퍼티를 사용 */
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
		setViewState(item.state)
	}

	private fun setViewState(state: AgonyRecordListItem.ItemState) {
		binding.progressbar.visibility =
			if (state is AgonyRecordListItem.ItemState.Loading) View.VISIBLE else View.GONE
		binding.editLayout.root.visibility =
			if (state is AgonyRecordListItem.ItemState.Editing) View.VISIBLE else View.INVISIBLE
		binding.swipeView.visibility =
			if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.INVISIBLE
		binding.swipeBackground.root.visibility =
			if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.INVISIBLE


		when (state) {
			is AgonyRecordListItem.ItemState.Editing -> {
				with(binding.editLayout) {
					agonyRecordEditTitleEt.setText(state.titleBeingEdited)
					agonyRecordEditContentEt.setText(state.contentBeingEdited)
					agonyRecordEditTitleEt.addTextChangedListener { text: Editable? ->
						state.titleBeingEdited = text.toString()
					}
					agonyRecordEditContentEt.addTextChangedListener { text: Editable? ->
						state.contentBeingEdited = text.toString()
					}
				}
			}

			is AgonyRecordListItem.ItemState.Success -> {
				isSwiped = state.isSwiped
				setViewHolderSwipeState(binding.swipeView, state.isSwiped)
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
