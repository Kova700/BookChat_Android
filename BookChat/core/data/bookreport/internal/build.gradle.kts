plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.bookreport.internal"
}

dependencies {
	implementation(project(":core:data:bookreport:external"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:data:common"))
}