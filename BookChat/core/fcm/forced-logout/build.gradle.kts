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
	ksp(libs.androidx.hilt.compiler)
	implementation(project(":core:domain"))
	implementation(project(":core:util"))
	implementation(project(":feature:login"))
}
