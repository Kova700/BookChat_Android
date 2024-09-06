plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.fcm_token.internal"
}

dependencies {
	implementation(project(":core:data:fcm-token:external"))
	implementation(project(":core:network:bookchat"))
	implementation(libs.firebase.messaging.ktx)
}
