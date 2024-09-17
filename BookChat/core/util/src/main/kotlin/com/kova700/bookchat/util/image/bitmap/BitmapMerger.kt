package com.kova700.bookchat.util.image.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getMergedBitmap(
	cacheRange: IntRange,
	bitmapCache: LruCache<Int, Bitmap>,
	bigBitmapWidth: Int,
	bigBitmapHeight: Int,
): Bitmap = withContext(Dispatchers.IO) {
	val bigBitmap = Bitmap.createBitmap(
		bigBitmapWidth,
		bigBitmapHeight,
		Bitmap.Config.ARGB_8888
	)
	val bigCanvas = Canvas(bigBitmap)
	bigCanvas.drawColor(Color.WHITE)

	var heightPosition = 0
	val paint = Paint()
	for (index in cacheRange.reversed()) {
		val bitmap = bitmapCache[index] ?: continue

		bigCanvas.drawBitmap(bitmap, 0f, heightPosition.toFloat(), paint)
		heightPosition += bitmap.height
		bitmap.recycle()
	}
	bigBitmap
}