plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.bookchat_token.internal"
}

dependencies {
	implementation(project(":core:datastore:bookchat-token"))
	implementation(project(":core:data:bookchat-token:external"))
}