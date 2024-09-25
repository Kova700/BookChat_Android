plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.oauth.internal.google.external"
}

dependencies {
	implementation(project(":core:data:oauth:external"))
	implementation(libs.bundles.google.login)
}