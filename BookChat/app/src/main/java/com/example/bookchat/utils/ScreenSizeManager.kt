package com.example.bookchat.utils

import android.util.DisplayMetrics
import com.example.bookchat.App

object ScreenSizeManager {
    private const val FLEX_BOX_BOOK_ITEM_WIDTH = 108

    private val displayMetrics: DisplayMetrics =
        App.instance.applicationContext.resources.displayMetrics
    private val deviceWidthPx = displayMetrics.widthPixels
    private val deviceWidthDp =
        deviceWidthPx / ((displayMetrics.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)
    private val flexBoxBookSpanSize =
        (deviceWidthDp / FLEX_BOX_BOOK_ITEM_WIDTH).toInt()

    fun getFlexBoxBookSpanItemCount(totalItemCount: Long) =
        (flexBoxBookSpanSize - (totalItemCount % flexBoxBookSpanSize).toInt()) % flexBoxBookSpanSize
}