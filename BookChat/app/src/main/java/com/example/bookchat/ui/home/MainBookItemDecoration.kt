package com.example.bookchat.ui.home

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MainBookItemDecoration @Inject constructor(
	@ApplicationContext applicationContext: Context
) : RecyclerView.ItemDecoration() {
	private val contextResources = applicationContext.resources
	private val displayMetrics: DisplayMetrics = contextResources.displayMetrics

	private fun getPxFromDp(dp: Int): Int =
		(dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State
	) {
		super.getItemOffsets(outRect, view, parent, state)
		val position = parent.getChildAdapterPosition(view)

		if (position == 0) {
			outRect.left = getPxFromDp(MAIN_BOOK_LEFT_OFFSET_DP)
		}
	}

	companion object {
		private const val MAIN_BOOK_LEFT_OFFSET_DP = 25
	}
}