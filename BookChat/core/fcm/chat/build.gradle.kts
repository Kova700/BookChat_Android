plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.fcm.chat"
}

dependencies {
	implementation(libs.bundles.workmanager)
	ksp(libs.androidx.hilt.compiler)
	implementation(project(":core:domain"))
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:appsetting:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:notification:chat:external"))
}
