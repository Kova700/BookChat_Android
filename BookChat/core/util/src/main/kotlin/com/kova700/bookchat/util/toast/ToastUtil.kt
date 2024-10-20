package com.kova700.bookchat.util.toast

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.makeToast(stringId: Int) {
	Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()
}

fun Fragment.makeToast(stringId: Int) {
	Toast.makeText(requireContext(), stringId, Toast.LENGTH_SHORT).show()
}