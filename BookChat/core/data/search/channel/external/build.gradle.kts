plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.search.channel.external"
}

dependencies {
	implementation(libs.generativeai)
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:channel:external"))
}