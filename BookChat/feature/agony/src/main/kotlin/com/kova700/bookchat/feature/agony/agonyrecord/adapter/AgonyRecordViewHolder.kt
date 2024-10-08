package com.kova700.bookchat.feature.agony.agonyrecord.adapter

import android.text.Editable
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kova700.bookchat.feature.agony.agonyrecord.AgonyRecordSwipeHelper
import com.kova700.bookchat.feature.agony.agonyrecord.model.AgonyRecordListItem
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordDataBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordFirstBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordHeaderBinding
import com.kova700.bookchat.feature.agony.databinding.ItemAgonyRecordPagingRetryBinding

sealed class AgonyRecordViewHolder(
	binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(agonyRecordListItem: AgonyRecordListItem)
}

class AgonyRecordHeaderViewHolder(
	private val binding: ItemAgonyRecordHeaderBinding,
	private val onHeaderEditBtnClick: (() -> Unit)?,
) : AgonyRecordViewHolder(binding) {

	init {
		binding.agonyTitleEditBtn.setOnClickListener {
			onHeaderEditBtnClick?.invoke()
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.Header
		with(binding) {
			agonyTitleTv.text = item.agony.title
		}
	}
}

class AgonyRecordFirstItemViewHolder(
	private val binding: ItemAgonyRecordFirstBinding,
	private val onFirstItemClick: (() -> Unit)?,
	private val onFirstItemEditCancelBtnClick: (() -> Unit)?,
	private val onFirstItemEditFinishBtnClick: (() -> Unit)?,
) : AgonyRecordViewHolder(binding) {

	init {
		with(binding) {
			agonyRecordFirstItemCv.setOnClickListener {
				onFirstItemClick?.invoke()
			}
			editLayout.agonyRecordFirstItemEditCancelBtn.setOnClickListener {
				onFirstItemEditCancelBtnClick?.invoke()
			}
			editLayout.agonyRecordFirstItemEditSubmitBtn.setOnClickListener {
				onFirstItemEditFinishBtnClick?.invoke()
			}
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.FirstItem
		setViewState(item.state)
	}

	private fun setViewState(state: AgonyRecordListItem.ItemState) {
		with(binding) {
			progressbar.visibility =
				if (state is AgonyRecordListItem.ItemState.Loading) View.VISIBLE else View.GONE
			editLayout.root.visibility =
				if (state is AgonyRecordListItem.ItemState.Editing) View.VISIBLE else View.GONE
			agonyRecordFirstItemCv.visibility =
				if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.GONE
		}

		if (state is AgonyRecordListItem.ItemState.Editing) {
			with(binding.editLayout) {
				agonyRecordFirstItemTitleEt.setText(state.editingTitle)
				agonyRecordFirstItemContentEt.setText(state.editingContent)
				agonyRecordFirstItemTitleEt.addTextChangedListener { text: Editable? ->
					state.editingTitle = text.toString()
				}
				agonyRecordFirstItemContentEt.addTextChangedListener { text: Editable? ->
					state.editingContent = text.toString()
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
		with(binding) {
			swipeView.setOnClickListener {
				onItemClick?.invoke(absoluteAdapterPosition)
			}
			editLayout.agonyRecordEditCancelBtn.setOnClickListener {
				onItemEditCancelBtnClick?.invoke(absoluteAdapterPosition)
			}
			editLayout.agonyRecordEditFinishTv.setOnClickListener {
				onItemEditFinishBtnClick?.invoke(absoluteAdapterPosition)
			}
			swipeBackground.background.setOnClickListener {
				onItemDeleteBtnClick?.invoke(absoluteAdapterPosition)
			}
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {
		val item = agonyRecordListItem as AgonyRecordListItem.Item
		setViewState(item)
	}

	private fun setViewState(item: AgonyRecordListItem.Item) {
		val state = item.state
		with(binding) {
			agonyRecordTitle.text = item.title
			agonyRecordContent.text = item.content
			agonyRecordCreatedAt.text = item.createdAt
			progressbar.visibility =
				if (state is AgonyRecordListItem.ItemState.Loading) View.VISIBLE else View.GONE
			editLayout.root.visibility =
				if (state is AgonyRecordListItem.ItemState.Editing) View.VISIBLE else View.GONE
			swipeView.visibility =
				if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.GONE
			swipeBackground.background.visibility =
				if (state is AgonyRecordListItem.ItemState.Success) View.VISIBLE else View.GONE
		}

		when (state) {
			is AgonyRecordListItem.ItemState.Editing -> {
				with(binding.editLayout) {
					agonyRecordEditTitleEt.setText(state.editingTitle)
					agonyRecordEditContentEt.setText(state.editingContent)
					agonyRecordEditContentEt.setSelection(state.editingContent.length)
					agonyRecordEditTitleEt.addTextChangedListener { text: Editable? ->
						state.editingTitle = text.toString()
					}
					agonyRecordEditContentEt.addTextChangedListener { text: Editable? ->
						state.editingContent = text.toString()
					}
				}
			}

			is AgonyRecordListItem.ItemState.Success -> {
				isSwiped = state.isSwiped
				setViewHolderSwipeState(binding.swipeView, state.isSwiped)
			}

			else -> Unit
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

class AgonyRecordPagingErrorViewHolder(
	private val binding: ItemAgonyRecordPagingRetryBinding,
	private val onClickPagingRetry: (() -> Unit)?,
) : AgonyRecordViewHolder(binding) {
	init {
		binding.retryBtn.setOnClickListener {
			onClickPagingRetry?.invoke()
		}
	}

	override fun bind(agonyRecordListItem: AgonyRecordListItem) {}
}