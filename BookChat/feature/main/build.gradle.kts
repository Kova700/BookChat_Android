plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.main"
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(libs.bundles.jetpack.navigation)
}