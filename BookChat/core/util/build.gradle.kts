plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.util"
}

dependencies {
	implementation(libs.bundles.androidx.default)
	implementation(libs.bundles.coil)
	implementation(project(":core:design-system"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:channel:external"))
}