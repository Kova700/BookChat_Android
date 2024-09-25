plugins {
	id("convention.android.library")
	id("convention.android.hilt")
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.kova700.bookchat.core.stomp.external"
}

dependencies {
	implementation(libs.kotlinx.serialization.json)
	implementation(project(":core:data:channel:external"))
}