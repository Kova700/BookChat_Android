plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.util"
}

dependencies {
	implementation(libs.bundles.androidx.default)
	// Glide
	implementation(libs.glide)
	ksp(libs.glide.compiler)

	implementation(project(":core:design-system"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:channel:external"))
}