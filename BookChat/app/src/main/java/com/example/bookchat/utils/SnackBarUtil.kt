package com.example.bookchat.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(textId: Int) {
	Snackbar.make(this, textId, Snackbar.LENGTH_SHORT).show()
}

fun View.showSnackBar(text: String) {
	Snackbar.make(this, text, Snackbar.LENGTH_SHORT).show()
}