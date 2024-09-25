package com.kova700.bookchat.core.notification.util.iconbuilder

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import com.kova700.bookchat.util.dp.dpToPx
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IconBuilder @Inject constructor(
	@ApplicationContext private val context: Context,
) {
	suspend fun buildIcon(
		imageUrl: String?,
		defaultImage: Bitmap,
	): IconCompat {
		if (imageUrl.isNullOrBlank()) return IconCompat.createWithBitmap(defaultImage)

		return imageUrl.getImageBitmap(
			context = context,
			imageWidthPx = 35.dpToPx(context),
			imageHeightPx = 35.dpToPx(context),
			roundedCornersRadiusPx = 14.dpToPx(context)
		)?.let(IconCompat::createWithBitmap)
			?: IconCompat.createWithBitmap(defaultImage)
	}
}