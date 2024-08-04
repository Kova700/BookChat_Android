package com.example.bookchat.notification.iconbuilder

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import com.example.bookchat.utils.dpToPx
import com.example.bookchat.utils.image.bitmap.getImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IconBuilderImpl @Inject constructor(
	@ApplicationContext private val context: Context,
) : IconBuilder {
	override suspend fun buildIcon(
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