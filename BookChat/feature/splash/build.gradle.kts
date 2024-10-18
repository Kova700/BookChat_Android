plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.splash"
	buildFeatures.viewBinding = true
}

dependencies {
	implementation(project(":core:data:bookchat-token:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:fcm-token:external"))
	implementation(project(":core:domain"))
	implementation(project(":core:util"))
	implementation(project(":core:remote-config"))
	implementation(project(":core:network-manager:external"))
}