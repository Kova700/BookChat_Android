package com.example.bookchat.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import com.example.bookchat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DialogSizeManager @Inject constructor(
	@ApplicationContext applicationContext: Context
) {
	private val contextResources = applicationContext.resources
	private val displayMetrics: DisplayMetrics = contextResources.displayMetrics
	private val deviceWidthPx: Int = displayMetrics.widthPixels

	private val DIALOG_EMPTY_AREA_MARGIN_HORIZONTAL_PX =
		contextResources.getDimensionPixelSize(R.dimen.bookshelf_dialog_empty_area_horizontal) * 2

	private val dialogWidthPx = deviceWidthPx - DIALOG_EMPTY_AREA_MARGIN_HORIZONTAL_PX

	fun setDialogSize(view: View) {
		view.layoutParams.width = dialogWidthPx
	}
}