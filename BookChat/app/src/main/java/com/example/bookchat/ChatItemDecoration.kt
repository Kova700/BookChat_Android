package com.example.bookchat

import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ChatItemDecoration : RecyclerView.ItemDecoration() {

    private val contextResources = App.instance.applicationContext.resources
    private val displayMetrics: DisplayMetrics = contextResources.displayMetrics

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val itemViewType = parent.adapter?.getItemViewType(position)

        when (itemViewType) {
            R.layout.item_chatting_mine,
            R.layout.item_chatting_other -> {
                outRect.bottom = getPxFromDp(USER_CHAT_BOTTOM_OFFSET_DP)
            }
            else -> {
                outRect.bottom = getPxFromDp(NOTICE_CHAT_BOTTOM_OFFSET_DP)
                outRect.top = getPxFromDp(NOTICE_CHAT_BOTTOM_OFFSET_DP)
            }
        }
    }

    private fun getPxFromDp(dp: Int): Int =
        (dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

    companion object {
        private const val USER_CHAT_BOTTOM_OFFSET_DP = 7
        private const val NOTICE_CHAT_BOTTOM_OFFSET_DP = 15
    }
}