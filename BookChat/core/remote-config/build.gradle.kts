plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.remoteconfig"
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(libs.bundles.androidx.default)
	implementation(platform(libs.firebase.bom))
	implementation(libs.firebase.analytics.ktx)
	implementation(libs.firebase.config.ktx)
}