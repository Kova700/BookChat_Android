package com.example.bookchat.notification

import android.content.Context
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
	suspend fun buildIcon(imageUrl: String?): IconCompat?
}

class IconBuilderImpl @Inject constructor(
	@ApplicationContext private val context: Context,
) : IconBuilder {
	override suspend fun buildIcon(imageUrl: String?): IconCompat? {
		if (imageUrl.isNullOrBlank()) return null

		val imageSize = 35.dpToPx(context)
		val roundedCornersRadiusPx = 14.dpToPx(context)

		return withContext(Dispatchers.IO) {
			runCatching {
				Glide.with(context)
					.asBitmap()
					.load(imageUrl)
					.apply(
						RequestOptions
							.overrideOf(imageSize)
							.transform(RoundedCorners(roundedCornersRadiusPx))
					)
					.submit()
					.get()
					?.let(IconCompat::createWithBitmap)
			}.getOrNull()
		}
	}

}