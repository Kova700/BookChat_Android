package com.kova700.bookchat.core.fcm.forced_logout

import android.app.Application

interface ForcedLogoutManager : Application.ActivityLifecycleCallbacks {
	fun start(application: Application)
	suspend fun onDeviceChanged()
	suspend fun onBookChatTokenExpired()
}