package com.kova700.bookchat.util.user

import android.content.Context
import android.graphics.Bitmap
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.util.image.bitmap.getImageBitmap

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

suspend fun UserDefaultProfileType?.getBitmap(context: Context): Bitmap {
	return getResId().getImageBitmap(context)
}
