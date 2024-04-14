package com.example.bookchat.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.ByteArrayOutputStream

fun Bitmap.compressToByteArray(): ByteArray {
	val stream = ByteArrayOutputStream()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		this.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, BITMAP_COMPRESS_QUALITY, stream)
		return stream.toByteArray()
	}
	this.compress(Bitmap.CompressFormat.WEBP, BITMAP_COMPRESS_QUALITY, stream)
	return stream.toByteArray()
}

fun ByteArray.toBitmap(): Bitmap {
	return BitmapFactory.decodeByteArray(this, 0, this.size)
}

private const val BITMAP_COMPRESS_QUALITY = 80 //임시 80