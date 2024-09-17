package com.kova700.bookchat.util.version

import android.content.Context

val Context.versionName: String
	get() = packageManager.getPackageInfo(packageName, 0)?.versionName.orEmpty()