plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.notificationinfo.internal"
}

dependencies {
	implementation(project(":core:datastore:notificationinfo"))
	implementation(project(":core:data:notificationinfo:external"))
}