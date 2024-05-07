package com.example.bookchat.utils

import android.content.Context
import android.util.DisplayMetrics
import android.widget.ImageView
import com.example.bookchat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MakeChannelImgSizeManager @Inject constructor(
	@ApplicationContext applicationContext: Context
) {
	private val contextResources = applicationContext.resources
	private val displayMetrics: DisplayMetrics = contextResources.displayMetrics
	private val deviceWidthPx: Int = displayMetrics.widthPixels

	private val DEFAULT_CHAT_ROOM_IMG_WIDTH_PX =
		contextResources.getDimensionPixelSize(R.dimen.make_chat_room_img_width_default)
	private val DEFAULT_CHAT_ROOM_IMG_HEIGHT_PX =
		contextResources.getDimensionPixelSize(R.dimen.make_chat_room_img_height_default)
	private val TARGET_SCREEN_WIDTH_PX =
		contextResources.getDimensionPixelSize(R.dimen.target_screen_width)

	private val scaleRate: Float = deviceWidthPx.toFloat() / TARGET_SCREEN_WIDTH_PX
	val chatRoomImgWidthPx = (DEFAULT_CHAT_ROOM_IMG_WIDTH_PX * scaleRate).toInt()
	val chatRoomImgHeightPx = (DEFAULT_CHAT_ROOM_IMG_HEIGHT_PX * scaleRate).toInt()

	fun setMakeChannelImgSize(view: ImageView) {
		with(view) {
			layoutParams.width = chatRoomImgWidthPx
			layoutParams.height = chatRoomImgHeightPx
		}
	}
}