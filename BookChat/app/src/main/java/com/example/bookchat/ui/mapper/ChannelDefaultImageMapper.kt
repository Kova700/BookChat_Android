package com.example.bookchat.ui.mapper

import android.content.Context
import android.graphics.Bitmap
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.utils.image.bitmap.getImageBitmap
import com.example.bookchat.utils.dpToPx

fun ChannelDefaultImageType.getResId() =
	when (this) {
		ChannelDefaultImageType.ONE -> R.drawable.default_chat_room_img1
		ChannelDefaultImageType.TWO -> R.drawable.default_chat_room_img2
		ChannelDefaultImageType.THREE -> R.drawable.default_chat_room_img3
		ChannelDefaultImageType.FOUR -> R.drawable.default_chat_room_img4
		ChannelDefaultImageType.FIVE -> R.drawable.default_chat_room_img5
		ChannelDefaultImageType.SIX -> R.drawable.default_chat_room_img6
		ChannelDefaultImageType.SEVEN -> R.drawable.default_chat_room_img7
	}

fun ChannelDefaultImageType.getBitmap(
	context: Context,
	imageSizePx: Int = 35.dpToPx(context),
	roundedCornersRadiusPx: Int = 14.dpToPx(context),
): Bitmap {
	return getResId().getImageBitmap(
		context = context,
		imageSizePx = imageSizePx,
		roundedCornersRadiusPx = roundedCornersRadiusPx
	)!!
}