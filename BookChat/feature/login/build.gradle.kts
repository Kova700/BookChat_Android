plugins {
	id("convention.android.feature")
}

android {
	namespace = "com.kova700.bookchat.feature.login"
}

dependencies {
	implementation(project(":core:domain"))
	implementation(project(":core:oauth:external"))
	implementation(project(":core:data:oauth:external"))
	implementation(project(":core:data:client:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:common"))
}