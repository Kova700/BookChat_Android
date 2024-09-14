plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.fcm.forcedLogout"
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(libs.bundles.androidx.default)
	implementation(libs.bundles.workmanager)
	implementation(project(":core:domain"))
	implementation(project(":feature:login"))
}
