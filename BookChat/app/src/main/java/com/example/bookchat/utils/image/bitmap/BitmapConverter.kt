package com.example.bookchat.utils.image.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val BITMAP_COMPRESS_QUALITY_DEFAULT = 80 //임시 80

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

//Int = Resource Id
suspend fun @receiver:DrawableRes Int.getImageBitmap(
	context: Context,
	imageSizePx: Int,
	roundedCornersRadiusPx: Int,
): Bitmap? {
	return withContext(Dispatchers.IO) {
		runCatching {
			Glide.with(context)
				.asBitmap()
				.load(
					AppCompatResources.getDrawable(context, this@getImageBitmap)
						?.toBitmap(
							width = imageSizePx,
							height = imageSizePx,
							config = Bitmap.Config.ARGB_8888
						)
				)
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

//Stirng = Image Url
suspend fun String.getImageBitmap(
	context: Context,
	imageSizePx: Int,
	roundedCornersRadiusPx: Int,
): Bitmap? {
	return withContext(Dispatchers.IO) {
		runCatching {
			Glide.with(context)
				.asBitmap()
				.load(this@getImageBitmap)
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

/** 너비를 변경하면 종횡비에 맞게 높이를 변환하여 반환하는 함수*/
suspend fun Bitmap.scaleDownWithAspectRatioForWidth(
	targetWidth: Int,
	maxHeight: Int? = null,
): Bitmap {
	if (targetWidth > width) return this

	val aspectRatio = width.toFloat() / height
	val newHeight = if (maxHeight == null) (targetWidth / aspectRatio).toInt()
	else minOf(maxHeight, (targetWidth / aspectRatio).toInt())
	return withContext(Dispatchers.IO) {
		Bitmap.createScaledBitmap(this@scaleDownWithAspectRatioForWidth, targetWidth, newHeight, true)
	}
}

suspend fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
	return withContext(Dispatchers.IO) {
		Bitmap.createScaledBitmap(this@resize, newWidth, newHeight, true)
	}
}