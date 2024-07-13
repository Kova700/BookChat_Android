package com.example.bookchat.ui.mapper

import android.content.Context
import android.graphics.Bitmap
import com.example.bookchat.R
import com.example.bookchat.domain.model.UserDefaultProfileType
import com.example.bookchat.utils.image.bitmap.getImageBitmap
import com.example.bookchat.utils.dpToPx

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

fun UserDefaultProfileType?.getBitmap(
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
