plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.oauth.internal"
}

dependencies {
	implementation(project(":core:data:oauth:external"))
}