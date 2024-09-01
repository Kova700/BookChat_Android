plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.deviceinfo.internal"
}

dependencies {
	implementation(project(":core:datastore:deviceinfo"))
	implementation(project(":core:data:deviceinfo:external"))
}