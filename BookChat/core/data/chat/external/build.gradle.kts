plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.chat.external"
}

dependencies {
	implementation(project(":core:data:user:external"))
}