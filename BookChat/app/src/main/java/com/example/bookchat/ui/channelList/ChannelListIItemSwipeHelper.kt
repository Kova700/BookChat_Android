package com.example.bookchat.ui.channelList

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.ui.channelList.adpater.ChannelListDataViewHolder
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class ChannelListIItemSwipeHelper @Inject constructor() : ItemTouchHelper.Callback() {

	private var currentTranslationX = 0F
	private var limitTranslationX = 0F

	private fun getSwipeView(viewHolder: RecyclerView.ViewHolder): View =
		(viewHolder as ChannelListDataViewHolder).itemView
			.findViewById(R.id.channel_list_swipe_view)

	private fun setSwiped(viewHolder: RecyclerView.ViewHolder, flag: Boolean) {
		(viewHolder as ChannelListDataViewHolder).setSwiped(flag)
	}

	private fun getSwiped(viewHolder: RecyclerView.ViewHolder): Boolean =
		(viewHolder as ChannelListDataViewHolder).getSwiped()

	private fun setLimitTranslationX(value: Float) {
		this.limitTranslationX = value
	}

	override fun getMovementFlags(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
	): Int {
		val swipeView = getSwipeView(viewHolder)
		setLimitTranslationX(swipeView.width.toFloat() * CHANNEL_LIST_ITEM_SWIPE_VIEW_PERCENT)

		val dragFlags = 0
		val swipeFlags = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
		return makeMovementFlags(dragFlags, swipeFlags)
	}

	/** 변위 dX : 우측 (+) ,좌측(-) */
	override fun onChildDraw(
		c: Canvas,
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		dX: Float,
		dY: Float,
		actionState: Int,
		isCurrentlyActive: Boolean,
	) {
		if (actionState != ACTION_STATE_SWIPE) return

		val swipeView = getSwipeView(viewHolder)
		val isSwiped = getSwiped(viewHolder)
		val x = getSwipeViewXPosition(dX / 1.5F, isSwiped, isCurrentlyActive)
		currentTranslationX = x
		getDefaultUIUtil().onDraw(c, recyclerView, swipeView, x, dY, actionState, isCurrentlyActive)
	}

	private fun getSwipeViewXPosition(
		dX: Float,
		isSwiped: Boolean,
		isCurrentlyActive: Boolean,
	): Float {
		val x = when {
			isSwiped -> if (isCurrentlyActive) max(0F, limitTranslationX + dX) else limitTranslationX
			else -> if (dX > 0F) dX else 0F
		}
		return min(x, limitTranslationX)
	}

	override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
		return defaultValue * 10
	}

	override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
		setSwiped(viewHolder, currentTranslationX >= limitTranslationX)
		return 2f
	}

	override fun clearView(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
	) {
		currentTranslationX = 0f
		getDefaultUIUtil().clearView(getSwipeView(viewHolder))
	}

	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		target: RecyclerView.ViewHolder,
	): Boolean = false

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

	companion object {
		const val CHANNEL_LIST_ITEM_SWIPE_VIEW_PERCENT = 0.4F
	}
}