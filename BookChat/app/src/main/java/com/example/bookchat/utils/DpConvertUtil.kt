package com.example.bookchat.utils

import android.content.Context

fun Int.dpToPx(context: Context): Int {
	val density = context.resources.displayMetrics.density
	return (this * density).toInt()
}

fun Int.pxToDp(context: Context): Int {
	val density = context.resources.displayMetrics.density
	return (this / density).toInt()
}