plugins {
	id("convention.android.library")
	id("convention.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.kova700.bookchat.core.fcm.service"
}

dependencies {
	implementation(project(":core:fcm:renew-fcm-token"))
	implementation(project(":core:fcm:forced-logout"))
	implementation(project(":core:fcm:chat"))
	implementation(libs.firebase.messaging.ktx)
	implementation(libs.kotlinx.serialization.json)
}
