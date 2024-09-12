plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.common"
}

dependencies {
	implementation(project(":core:network:bookchat"))
}