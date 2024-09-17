package com.kova700.bookchat.util.toast

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Activity.makeToast(stringId: Int) {
	Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()
}

fun Fragment.makeToast(stringId: Int) {
	Toast.makeText(requireContext(), stringId, Toast.LENGTH_SHORT).show()
}