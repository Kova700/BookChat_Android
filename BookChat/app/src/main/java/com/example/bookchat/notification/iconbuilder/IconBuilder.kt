package com.example.bookchat.notification.iconbuilder

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat

interface IconBuilder {
	suspend fun buildIcon(
		imageUrl: String?,
		defaultImage: Bitmap,
	): IconCompat
}