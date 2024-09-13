plugins {
	id("convention.android.library")
	id("convention.android.hilt")
}

android {
	namespace = "com.kova700.bookchat.core.data.chat.internal"
}

dependencies {
	implementation(project(":core:data:chat:external"))
	implementation(project(":core:data:user:external"))
	implementation(project(":core:database:chatting:external"))
	implementation(project(":core:network:bookchat"))
}