package com.kova700.bookchat.util.snackbar

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.kova700.bookchat.core.design_system.R

fun View.showSnackBar(
	textId: Int,
	anchor: View? = null,
) {
	if (parent == null) return
	getSnackBar(
		text = context.getString(textId),
		anchor = anchor
	).show()
}

fun View.showSnackBar(
	text: String,
	anchor: View? = null,
) {
	if (parent == null) return
	getSnackBar(
		text = text,
		anchor = anchor
	).show()
}

private fun View.getSnackBar(
	text: String,
	anchor: View? = null,
): Snackbar {
	return Snackbar
		.make(this, text, Snackbar.LENGTH_SHORT)
		.apply {
			anchorView = anchor
			animationMode = ANIMATION_MODE_SLIDE
			view.background = AppCompatResources.getDrawable(context, R.drawable.snackbar_background)
			setTextColor(context.getColor(R.color.white))
			view.layoutParams = view.getSnackBarLayoutParam()
		}
}

private fun View.getSnackBarLayoutParam(): ViewGroup.LayoutParams? {
	return when (val params = layoutParams) {
		is FrameLayout.LayoutParams -> {
			params.apply {
				gravity = Gravity.BOTTOM or Gravity.CENTER
				width = ViewGroup.LayoutParams.WRAP_CONTENT
			}
		}

		is CoordinatorLayout.LayoutParams -> {
			params.apply {
				gravity = Gravity.BOTTOM or Gravity.CENTER
				width = ViewGroup.LayoutParams.WRAP_CONTENT
			}
		}

		else -> null
	}
}