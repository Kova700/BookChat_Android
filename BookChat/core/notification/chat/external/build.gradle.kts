plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.notification.chat.external"
}

dependencies {
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:chat:external"))
}