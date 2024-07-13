package com.example.bookchat.notification

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.image.bitmap.getImageBitmap
import com.example.bookchat.utils.dpToPx
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface IconBuilder {
	suspend fun buildIcon(
		imageUrl: String?,
		defaultImage: Bitmap,
	): IconCompat
}

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
			imageSizePx = 35.dpToPx(context),
			roundedCornersRadiusPx = 14.dpToPx(context)
		)?.let(IconCompat::createWithBitmap)
			?: IconCompat.createWithBitmap(defaultImage)
	}
}