package com.kova700.bookchat.core.notification.util.iconbuilder

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import com.kova700.bookchat.util.dp.dpToPx
import com.kova700.bookchat.util.image.bitmap.cropCenterSquare
import com.kova700.bookchat.util.image.bitmap.getImageBitmap
import com.kova700.bookchat.util.image.bitmap.resize
import com.kova700.bookchat.util.image.bitmap.setRoundedCorner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IconBuilder @Inject constructor(
	@ApplicationContext private val context: Context,
) {
	suspend fun buildIcon(
		imageUrl: String?,
		defaultImage: Bitmap,
	): IconCompat {
		if (imageUrl.isNullOrBlank()) return defaultImage.getIcon()

		return imageUrl
			.getImageBitmap(context)
			?.getIcon()
			?: defaultImage.getIcon()
	}

	private suspend fun Bitmap.getIcon(): IconCompat {
		return cropCenterSquare()
			.resize(35.dpToPx(context))
			.setRoundedCorner(14.dpToPx(context).toFloat())
			.let(IconCompat::createWithBitmap)
	}
}