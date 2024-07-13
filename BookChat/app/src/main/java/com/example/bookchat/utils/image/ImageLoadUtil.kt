package com.example.bookchat.utils.image

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.bookchat.R
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.UserDefaultProfileType
import com.example.bookchat.ui.mapper.getResId

fun ImageView.loadUrl(
	url: String?,
	placeholderResId: Int = R.drawable.loading_img,
	errorResId: Int = R.drawable.error_img,
) {
	if (url.isNullOrEmpty()) return

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
		.fitCenter()
		.placeholder(placeholderResId)
		.error(errorResId)
		.into(this)
}

fun ImageView.loadUserProfile(
	imageUrl: String?,
	userDefaultProfileType: UserDefaultProfileType?,
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
	byteArray: ByteArray?,
) {
	if (byteArray != null) {
		loadByteArray(byteArray)
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
	byteArray: ByteArray?,
) {
	if (byteArray != null) {
		loadByteArray(byteArray)
		return
	}
	loadChannelProfile(imageUrl, channelDefaultImageType)
}