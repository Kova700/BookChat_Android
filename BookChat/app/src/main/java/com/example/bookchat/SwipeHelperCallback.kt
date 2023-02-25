package com.example.bookchat

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.adapter.agonyrecord.AgonyRecordDataItemAdapter
import kotlin.math.max
import kotlin.math.min

class SwipeHelperCallback : ItemTouchHelper.Callback() {
    private var currentDx = 0F
    private var limitDx = 0F

    private fun getSwipeView(viewHolder: RecyclerView.ViewHolder): View =
        (viewHolder as AgonyRecordDataItemAdapter.AgonyRecordDataItemViewHolder).itemView
            .findViewById(R.id.swipe_view)

    private fun setSwiped(viewHolder: RecyclerView.ViewHolder, flag :Boolean) =
        (viewHolder as AgonyRecordDataItemAdapter.AgonyRecordDataItemViewHolder).setSwiped(flag)

    private fun getSwiped(viewHolder: RecyclerView.ViewHolder) :Boolean =
        (viewHolder as AgonyRecordDataItemAdapter.AgonyRecordDataItemViewHolder).getSwiped()

    private fun setLimitDx(value: Float) {
        this.limitDx = value
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeView = getSwipeView(viewHolder)
        setLimitDx(swipeView.width.toFloat() * SWIPE_VIEW_PERCENT)

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
        isCurrentlyActive: Boolean
        ) {
        if (actionState != ACTION_STATE_SWIPE) return

        val swipeView = getSwipeView(viewHolder)
        val isSwiped = getSwiped(viewHolder)
        val x = getSwipeViewXPosition(dX/1.5F, isSwiped, isCurrentlyActive)
        currentDx = x
        getDefaultUIUtil().onDraw(c, recyclerView, swipeView, x, dY, actionState, isCurrentlyActive)
    }

    private fun getSwipeViewXPosition(
        dX: Float,
        isSwiped: Boolean,
        isCurrentlyActive: Boolean
    ): Float {
        val x =
        if (isSwiped) {
            if (isCurrentlyActive) max(0F, limitDx + dX) else limitDx
        } else {
            if (dX > 0F) dX else 0F
        }

        return min(x, limitDx)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        setSwiped(viewHolder,currentDx >= limitDx)
        return 2f
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        currentDx = 0f
        getDefaultUIUtil().clearView(getSwipeView(viewHolder))
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder): Boolean  { return false }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

    companion object {
        const val SWIPE_VIEW_PERCENT = 0.3F
    }
}