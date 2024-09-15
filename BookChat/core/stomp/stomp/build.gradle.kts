plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.stomp.stomp"
}

dependencies {
	implementation(libs.bundles.krossbow.stomp)
	implementation(project(":core:util"))
}