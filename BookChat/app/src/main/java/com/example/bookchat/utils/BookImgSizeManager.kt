package com.example.bookchat.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import com.example.bookchat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BookImgSizeManager @Inject constructor(
	@ApplicationContext applicationContext: Context
) {
	private val contextResources = applicationContext.resources
	private val displayMetrics: DisplayMetrics = contextResources.displayMetrics
	private val deviceWidthPx: Int = displayMetrics.widthPixels

	private val BOOK_ITEM_MARGIN_HORIZONTAL_PX =
		contextResources.getDimensionPixelSize(R.dimen.flex_box_book_item_margin_horizontal) * 2
	private val BOOK_ITEM_MARGIN_VERTICAL_PX =
		contextResources.getDimensionPixelSize(R.dimen.flex_box_book_item_margin_vertical) * 2
	private val FLEX_BOX_MARGIN_HORIZONTAL_PX =
		contextResources.getDimensionPixelSize(R.dimen.flex_box_margin_horizontal) * 2

	private val DEFAULT_BOOK_IMG_WIDTH_PX =
		contextResources.getDimensionPixelSize(R.dimen.book_img_width_default)
	private val DEFAULT_BOOK_IMG_HEIGHT_PX =
		contextResources.getDimensionPixelSize(R.dimen.book_img_height_default)

	private val DEFAULT_BOOK_ITEM_WIDTH_PX =
		DEFAULT_BOOK_IMG_WIDTH_PX + BOOK_ITEM_MARGIN_HORIZONTAL_PX
	private val DEFAULT_BOOK_ITEM_HEIGHT_PX =
		DEFAULT_BOOK_IMG_HEIGHT_PX + BOOK_ITEM_MARGIN_VERTICAL_PX

	private val flexBoxWidthPx = deviceWidthPx - FLEX_BOX_MARGIN_HORIZONTAL_PX
	val flexBoxBookSpanSize: Int = flexBoxWidthPx / DEFAULT_BOOK_ITEM_WIDTH_PX

	val bookItemWidthPx = (flexBoxWidthPx / flexBoxBookSpanSize)

	val bookImgWidthPx =
		bookItemWidthPx - BOOK_ITEM_MARGIN_HORIZONTAL_PX

	private val scaleRate: Float =
		(bookImgWidthPx.toFloat() / DEFAULT_BOOK_IMG_WIDTH_PX)

	val bookImgHeightPx =
		(DEFAULT_BOOK_IMG_HEIGHT_PX * scaleRate).toInt()

	fun getFlexBoxDummyItemCount(totalItemCount: Int) =
		(flexBoxBookSpanSize - (totalItemCount % flexBoxBookSpanSize)) % flexBoxBookSpanSize

	fun setBookImgSize(view: View) {
		with(view) {
			layoutParams.width = bookImgWidthPx
			layoutParams.height = bookImgHeightPx
		}
	}

	fun getPxFromDp(dp: Int): Int =
		(dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

	fun getDpFromPx(px: Int): Int =
		(px / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}