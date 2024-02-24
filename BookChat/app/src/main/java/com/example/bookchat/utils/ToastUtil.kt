package com.example.bookchat.utils

import android.widget.Toast
import com.example.bookchat.App

fun makeToast(stringId: Int) {
	Toast.makeText(App.instance.applicationContext, stringId, Toast.LENGTH_SHORT).show()
}