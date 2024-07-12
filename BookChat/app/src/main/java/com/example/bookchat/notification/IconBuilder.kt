package com.example.bookchat.notification

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookchat.utils.dpToPx
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
			imageSizePx = 35.dpToPx(context),
			roundedCornersRadiusPx = 14.dpToPx(context)
		)?.let(IconCompat::createWithBitmap)
			?: IconCompat.createWithBitmap(defaultImage)
	}

	private suspend fun String.getImageBitmap(
		imageSizePx: Int,
		roundedCornersRadiusPx: Int,
	): Bitmap? {
		return withContext(Dispatchers.IO) {
			runCatching {
				Glide.with(context)
					.asBitmap()
					.load(this)
					.apply(
						RequestOptions
							.overrideOf(imageSizePx)
							.transform(RoundedCorners(roundedCornersRadiusPx))
					)
					.submit()
					.get()
			}.getOrNull()
		}
	}

}