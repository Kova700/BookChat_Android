package com.kova700.bookchat.feature.agony.agony.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.kova700.bookchat.util.dp.dpToPx
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AgonyItemDecorator @Inject constructor(
	@ApplicationContext private val context: Context,
) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State,
	) {
		super.getItemOffsets(outRect, view, parent, state)
		val position = parent.getChildAdapterPosition(view)
		if (position < 1) return

		when {
			(position % 2) == 0 -> {
				outRect.right = HORIZONTAL_OUTSIDE_OFFSET_DP.dpToPx(context)
				outRect.left = HORIZONTAL_INNER_OFFSET_DP.dpToPx(context)
			}

			else -> {
				outRect.right = HORIZONTAL_INNER_OFFSET_DP.dpToPx(context)
				outRect.left = HORIZONTAL_OUTSIDE_OFFSET_DP.dpToPx(context)
			}
		}
	}

	companion object {
		private const val HORIZONTAL_INNER_OFFSET_DP = 10
		private const val HORIZONTAL_OUTSIDE_OFFSET_DP = 20
	}
}