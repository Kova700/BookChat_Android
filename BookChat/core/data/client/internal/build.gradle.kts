plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.client.internal"
}

dependencies {
	implementation(libs.bundles.retrofit)
	implementation(platform(libs.okhttp.bom))
	implementation(project(":core:oauth:external"))
	implementation(project(":core:data:oauth:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:fcm-token:external"))
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:common"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:network:util"))
}