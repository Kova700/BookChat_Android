package com.kova700.bookchat.util.image.image

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kova700.bookchat.core.data.channel.external.model.ChannelDefaultImageType
import com.kova700.bookchat.core.data.user.external.model.UserDefaultProfileType
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.util.channel.getResId
import com.kova700.bookchat.util.user.getResId

fun ImageView.loadUrl(
	url: String?,
	placeholderResId: Int = R.drawable.loading_img,
	errorResId: Int = R.drawable.error_img,
) {
	if (url.isNullOrEmpty()) {
		loadResId(R.drawable.empty_img)
		return
	}

	Glide.with(context)
		.load(url)
		.placeholder(placeholderResId)
		.error(errorResId)
		.into(this)
}

fun ImageView.loadBitmap(
	bitmap: Bitmap?,
	placeholderResId: Int = R.drawable.loading_img,
	errorResId: Int = R.drawable.error_img,
) {
	if (bitmap == null) return

	Glide.with(context)
		.load(bitmap)
		.placeholder(placeholderResId)
		.error(errorResId)
		.into(this)
}

fun ImageView.loadByteArray(
	byteArray: ByteArray?,
	placeholderResId: Int = R.drawable.loading_img,
	errorResId: Int = R.drawable.error_img,
) {
	if (byteArray == null || byteArray.isEmpty()) return
	val key = byteArray.contentHashCode()
	if (tag == key) return

	tag = key
	Glide.with(context)
		.asBitmap()
		.load(byteArray)
		.placeholder(placeholderResId)
		.error(errorResId)
		.into(this)
}

fun ImageView.loadResId(
	resId: Int,
	placeholderResId: Int = R.drawable.loading_img,
	errorResId: Int = R.drawable.error_img,
) {
	Glide.with(context)
		.load(resId)
		.placeholder(placeholderResId)
		.error(errorResId)
		.into(this)
}

fun ImageView.loadUserProfile(
	imageUrl: String?,
	userDefaultProfileType: UserDefaultProfileType? = null,
) {
	if (imageUrl.isNullOrBlank().not()) {
		loadUrl(imageUrl)
		return
	}
	loadResId(userDefaultProfileType.getResId())
}

fun ImageView.loadChangedUserProfile(
	imageUrl: String?,
	userDefaultProfileType: UserDefaultProfileType?,
	bitmap: Bitmap?,
) {
	if (bitmap != null) {
		loadBitmap(bitmap)
		return
	}
	loadUserProfile(imageUrl, userDefaultProfileType)
}

fun ImageView.loadChannelProfile(
	imageUrl: String?,
	channelDefaultImageType: ChannelDefaultImageType,
) {
	if (imageUrl.isNullOrBlank().not()) {
		loadUrl(imageUrl)
		return
	}
	loadResId(channelDefaultImageType.getResId())
}

fun ImageView.loadChangedChannelProfile(
	imageUrl: String?,
	channelDefaultImageType: ChannelDefaultImageType,
	bitmap: Bitmap?,
) {
	if (bitmap != null) {
		loadBitmap(bitmap)
		return
	}
	loadChannelProfile(imageUrl, channelDefaultImageType)
}