plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.notification.util"
}

dependencies {
	implementation(project(":core:util"))
}