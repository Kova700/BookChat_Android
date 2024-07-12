package com.example.bookchat.ui.mapper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.bookchat.R
import com.example.bookchat.domain.model.UserDefaultProfileType

fun UserDefaultProfileType?.getResId() =
	when (this) {
		null,
		UserDefaultProfileType.ONE,
		-> R.drawable.default_profile_img1

		UserDefaultProfileType.TWO -> R.drawable.default_profile_img2
		UserDefaultProfileType.THREE -> R.drawable.default_profile_img3
		UserDefaultProfileType.FOUR -> R.drawable.default_profile_img4
		UserDefaultProfileType.FIVE -> R.drawable.default_profile_img5
	}

fun UserDefaultProfileType?.getBitmap(context: Context): Bitmap =
	BitmapFactory.decodeResource(context.resources, getResId())
