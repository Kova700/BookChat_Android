package com.example.bookchat.fcm.forcedlogout

import android.app.Application

interface ForcedLogoutManager : Application.ActivityLifecycleCallbacks {
	fun start(application: Application)
	suspend fun onLogoutMessageReceived()
}