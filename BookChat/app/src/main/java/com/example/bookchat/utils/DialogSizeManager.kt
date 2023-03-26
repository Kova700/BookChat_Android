package com.example.bookchat.utils

import android.util.DisplayMetrics
import com.example.bookchat.App
import com.example.bookchat.R

object DialogSizeManager {
    private val contextResources = App.instance.applicationContext.resources
    private val displayMetrics: DisplayMetrics = contextResources.displayMetrics
    private val deviceWidthPx: Int = displayMetrics.widthPixels

    private val DIALOG_EMPTY_AREA_MARGIN_HORIZONTAL_PX =
        contextResources.getDimensionPixelSize(R.dimen.bookshelf_dialog_empty_area_horizontal) * 2

    val dialogWidthPx = deviceWidthPx - DIALOG_EMPTY_AREA_MARGIN_HORIZONTAL_PX
}