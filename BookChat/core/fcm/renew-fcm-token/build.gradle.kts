plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.fcm.renew_fcm_token"
}

dependencies {
	implementation(libs.bundles.workmanager)
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:fcm-token:external"))
}
