package com.example.bookchat.utils

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.example.bookchat.R
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(textId: Int) {
	getSnackBar(context.getString(textId)).show()
}

fun View.showSnackBar(text: String) {
	getSnackBar(text).show()
}

private fun View.getSnackBar(text: String): Snackbar {
	return Snackbar
		.make(this, text, Snackbar.LENGTH_SHORT)
		.setAnchorView(this)
		.apply {
			animationMode = ANIMATION_MODE_SLIDE
			view.background = AppCompatResources.getDrawable(context, R.drawable.snackbar_background)
			setTextColor(context.getColor(R.color.white))
			view.layoutParams = view.getSnackBarLayoutParam()
		}
}

private fun View.getSnackBarLayoutParam(): FrameLayout.LayoutParams {
	return (layoutParams as (FrameLayout.LayoutParams)).apply {
		gravity = Gravity.BOTTOM or Gravity.CENTER
		width = FrameLayout.LayoutParams.WRAP_CONTENT
	}
}