package com.example.bookchat.ui.home.book.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.utils.dpToPx
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HomeBookItemDecoration @Inject constructor(
	@ApplicationContext private val applicationContext: Context,
) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(
		outRect: Rect,
		view: View,
		parent: RecyclerView,
		state: RecyclerView.State,
	) {
		super.getItemOffsets(outRect, view, parent, state)
		val position = parent.getChildAdapterPosition(view)
		if (position == 0) outRect.left = MAIN_BOOK_LEFT_OFFSET_DP.dpToPx(applicationContext)
	}

	companion object {
		private const val MAIN_BOOK_LEFT_OFFSET_DP = 25
	}
}