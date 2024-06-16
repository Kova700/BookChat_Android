package com.example.bookchat.utils.bitmap

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream

fun Bitmap.compressToByteArray(compressQuality: Int = BITMAP_COMPRESS_QUALITY): ByteArray {
	val stream = ByteArrayOutputStream()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		this.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, compressQuality, stream)
		return stream.toByteArray()
	}
	this.compress(Bitmap.CompressFormat.WEBP, BITMAP_COMPRESS_QUALITY, stream)
	return stream.toByteArray()
}

private const val BITMAP_COMPRESS_QUALITY = 80 //임시 80