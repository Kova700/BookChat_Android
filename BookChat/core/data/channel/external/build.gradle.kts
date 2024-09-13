plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.channel.external"
}

dependencies {
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:data:chat:external"))
}