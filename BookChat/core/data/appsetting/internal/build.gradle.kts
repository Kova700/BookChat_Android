plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.appsetting.internal"
}

dependencies {
	implementation(project(":core:datastore:appsetting"))
	implementation(project(":core:data:appsetting:external"))
}