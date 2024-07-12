package com.example.bookchat.ui.mapper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChannelDefaultImageType

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

fun ChannelDefaultImageType.getBitmap(context: Context): Bitmap =
	BitmapFactory.decodeResource(context.resources, getResId())
