package com.kova700.bookchat.util.channel

import android.content.Context
import android.graphics.Bitmap
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.util.image.bitmap.getImageBitmap

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

suspend fun ChannelDefaultImageType.getBitmap(context: Context): Bitmap {
	return getResId().getImageBitmap(context)
}