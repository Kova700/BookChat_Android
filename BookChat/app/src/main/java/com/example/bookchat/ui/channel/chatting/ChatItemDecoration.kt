package com.example.bookchat.ui.channel.chatting

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChatItemDecoration @Inject constructor(
	@ApplicationContext applicationContext: Context,
) : RecyclerView.ItemDecoration() {

	private val contextResources = applicationContext.resources
	private val displayMetrics: DisplayMetrics = contextResources.displayMetrics

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State,
	) {
		super.getItemOffsets(outRect, view, parent, state)
		val position = parent.getChildAdapterPosition(view)
		if (position == RecyclerView.NO_POSITION) return

		when (parent.adapter?.getItemViewType(position)) {
			R.layout.item_chatting_mine,
			R.layout.item_chatting_other -> {
				outRect.bottom = getPxFromDp(USER_CHAT_BOTTOM_OFFSET_DP)
			}

			R.layout.item_chatting_date -> {
				outRect.bottom = getPxFromDp(DATE_CHAT_TOP_BOTTOM_OFFSET_DP)
				outRect.top = getPxFromDp(DATE_CHAT_TOP_BOTTOM_OFFSET_DP)
			}

			else -> {
				outRect.bottom = getPxFromDp(NOTICE_CHAT_TOP_BOTTOM_OFFSET_DP)
				outRect.top = getPxFromDp(NOTICE_CHAT_TOP_BOTTOM_OFFSET_DP)
			}
		}
	}

	private fun getPxFromDp(dp: Int): Int =
		(dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

	companion object {
		private const val USER_CHAT_BOTTOM_OFFSET_DP = 7
		private const val NOTICE_CHAT_TOP_BOTTOM_OFFSET_DP = 10
		private const val DATE_CHAT_TOP_BOTTOM_OFFSET_DP = 15
	}
}