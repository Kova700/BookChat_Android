package com.example.bookchat.utils.image.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.ByteArrayOutputStream

private const val BITMAP_COMPRESS_QUALITY = 80 //임시 80

fun Bitmap.compressToByteArray(compressQuality: Int = BITMAP_COMPRESS_QUALITY): ByteArray {
	val stream = ByteArrayOutputStream()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		compress(Bitmap.CompressFormat.WEBP_LOSSLESS, compressQuality, stream)
		return stream.toByteArray()
	}
	compress(Bitmap.CompressFormat.WEBP, BITMAP_COMPRESS_QUALITY, stream)
	return stream.toByteArray()
}

//Int = Resource Id
fun @receiver:DrawableRes Int.getImageBitmap(
	context: Context,
	imageSizePx: Int,
	roundedCornersRadiusPx: Int,
): Bitmap? {
	return runCatching {
		Glide.with(context)
			.asBitmap()
			.load(
				AppCompatResources.getDrawable(context, this)
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

//Stirng = Image Url
fun String.getImageBitmap(
	context: Context,
	imageSizePx: Int,
	roundedCornersRadiusPx: Int,
): Bitmap? {
	return runCatching {
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