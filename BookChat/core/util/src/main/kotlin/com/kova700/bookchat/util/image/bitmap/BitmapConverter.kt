package com.kova700.bookchat.util.image.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val BITMAP_COMPRESS_QUALITY_DEFAULT = 80

suspend fun Bitmap.compressToByteArray(compressQuality: Int = BITMAP_COMPRESS_QUALITY_DEFAULT): ByteArray {
	return withContext(Dispatchers.IO) {
		val stream = ByteArrayOutputStream()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			compress(Bitmap.CompressFormat.WEBP_LOSSLESS, compressQuality, stream)
			stream.toByteArray()
		}
		compress(Bitmap.CompressFormat.WEBP, BITMAP_COMPRESS_QUALITY_DEFAULT, stream)
		stream.toByteArray()
	}
}

fun ByteArray.toBitmap(): Bitmap? {
	return BitmapFactory.decodeByteArray(this, 0, this.size)
}

//Int = Resource Id
suspend fun @receiver:DrawableRes Int.getImageBitmap(
	context: Context,
): Bitmap {
	return withContext(Dispatchers.IO) {
		Glide.with(context)
			.asBitmap()
			.load(
				AppCompatResources
					.getDrawable(context, this@getImageBitmap)
					?.toBitmap(config = Bitmap.Config.ARGB_8888)
			).submit()
			.get()
	}
}

//Stirng = Image Url
suspend fun String.getImageBitmap(
	context: Context,
): Bitmap? {
	return withContext(Dispatchers.IO) {
		runCatching {
			Glide.with(context)
				.asBitmap()
				.load(this@getImageBitmap)
				.submit()
				.get()
		}.getOrNull()
	}
}

/** 기본적으로 가로,세로 최대 길이를 제한하지 않고
 * 총 픽셀 수 ( 가로 x 세로 ) 가 최대 치 이상인 경우에만 제한
 * 이미지 축소를 할 때는 (가로 , 세로) 중 더 긴 길이를 기준으로 축소 */
suspend fun Bitmap.scaleDownWithAspectRatio(
	maxWidth: Int,
	maxHeight: Int,
): Bitmap {
	if (maxWidth * maxHeight > width * height) return this

	return when {
		width > height -> scaleDownWithAspectRatioForWidth(maxWidth, maxHeight)
		else -> scaleDownWithAspectRatioForHeight(maxWidth, maxHeight)
	}
}

/** 너비를 변경하면 종횡비에 맞게 높이를 변환하여 반환하는 함수*/
private suspend fun Bitmap.scaleDownWithAspectRatioForWidth(
	targetWidth: Int,
	maxHeight: Int? = null,
): Bitmap {
	if (targetWidth > width) return this

	val aspectRatio = width.toFloat() / height
	val newHeight =
		if (maxHeight == null) (targetWidth / aspectRatio).toInt()
		else minOf(maxHeight, (targetWidth / aspectRatio).toInt())
	return withContext(Dispatchers.IO) {
		Bitmap.createScaledBitmap(
			this@scaleDownWithAspectRatioForWidth, targetWidth, newHeight, true
		)
	}
}

/** 높이를 변경하면 종횡비에 맞게 너비를 변환하여 반환하는 함수*/
private suspend fun Bitmap.scaleDownWithAspectRatioForHeight(
	targetHeight: Int,
	maxWidth: Int? = null,
): Bitmap {
	if (targetHeight > height) return this

	val aspectRatio = height.toFloat() / width
	val newWidth =
		if (maxWidth == null) (targetHeight / aspectRatio).toInt()
		else minOf(maxWidth, (targetHeight / aspectRatio).toInt())
	return withContext(Dispatchers.IO) {
		Bitmap.createScaledBitmap(
			this@scaleDownWithAspectRatioForHeight, newWidth, targetHeight, true
		)
	}
}

suspend fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
	return withContext(Dispatchers.IO) {
		Bitmap.createScaledBitmap(this@resize, newWidth, newHeight, true)
	}
}

suspend fun Bitmap.resize(size: Int): Bitmap {
	return resize(size, size)
}

suspend fun Bitmap.cropCenterSquare(): Bitmap {
	if (width == height) return this
	val minSize = minOf(width, height)
	val x = (width - minSize) / 2
	val y = (height - minSize) / 2
	return withContext(Dispatchers.IO) {
		Bitmap.createBitmap(this@cropCenterSquare, x, y, minSize, minSize)
	}
}

fun Bitmap.setRoundedCorner(roundPx: Float): Bitmap {
	val output = Bitmap.createBitmap(
		width,
		height,
		Bitmap.Config.ARGB_8888
	)

	val canvas = Canvas(output)
	val paint = Paint()
	val rect = Rect(0, 0, width, height)
	val rectF = RectF(rect)

	paint.isAntiAlias = true
	canvas.drawARGB(0, 0, 0, 0)
	canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
	paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
	canvas.drawBitmap(this, rect, rect, paint)
	return output
}