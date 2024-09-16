plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.network.util"
}

dependencies {
	implementation(libs.bundles.retrofit)
	implementation(platform(libs.okhttp.bom))
}