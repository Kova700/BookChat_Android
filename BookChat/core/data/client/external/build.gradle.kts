plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.client.external"
}

dependencies {
	implementation(project(":core:data:oauth:external"))
	implementation(project(":core:data:fcm-token:external"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:user:external"))
}