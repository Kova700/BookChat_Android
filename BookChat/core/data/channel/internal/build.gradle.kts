plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.channel.internal"
}

dependencies {
	implementation(libs.kotlinx.serialization.json)
	implementation(project(":core:data:channel:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:search:book:external"))
	implementation(project(":core:database:chatting:external"))
	implementation(project(":core:network:bookchat"))
	implementation(project(":core:data:client:external"))
}