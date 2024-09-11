plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.agonyrecord.internal"
}

dependencies {
	implementation(project(":core:data:agonyrecord:external"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:data:common"))
}