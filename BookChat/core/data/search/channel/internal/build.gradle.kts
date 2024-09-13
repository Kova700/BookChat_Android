plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.search.channel.internal"
}

dependencies {
	implementation(project(":core:data:search:channel:external"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:data:user:external"))
}